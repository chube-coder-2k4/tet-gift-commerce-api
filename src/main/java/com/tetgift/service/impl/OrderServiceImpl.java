package com.tetgift.service.impl;

import com.tetgift.dto.request.OrderRequest;
import com.tetgift.dto.response.OrderItemResponse;
import com.tetgift.dto.response.OrderResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.enums.OrderStatus;
import com.tetgift.exception.ForBiddenException;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.Users;
import com.tetgift.model.entity.*;
import com.tetgift.repository.jpa.CartRepository;
import com.tetgift.repository.jpa.DiscountRepository;
import com.tetgift.repository.jpa.OrderRepository;
import com.tetgift.service.OrderService;
import com.tetgift.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final DiscountRepository discountRepository;
    private final AuthenticationUtils authenticationUtils;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Users user = authenticationUtils.getCurrentUser();
        if (user == null)
            throw new ForBiddenException("User not authenticated");

        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new InvalidDataException("Cart is empty");
        }

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .vatCompanyName(request.getVatCompanyName())
                .vatTaxCode(request.getVatTaxCode())
                .vatPhone(request.getVatPhone())
                .vatAddress(request.getVatAddress())
                .build();

        // Snapshot cart items into order items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (CartItemEntity cartItem : cart.getCartItems()) {
            BigDecimal price;
            if ("PRODUCT".equals(cartItem.getItemType()) && cartItem.getProduct() != null) {
                price = cartItem.getProduct().getPrice();
            } else if ("BUNDLE".equals(cartItem.getItemType()) && cartItem.getBundle() != null) {
                price = cartItem.getBundle().getPrice();
            } else {
                continue;
            }

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .itemType(cartItem.getItemType())
                    .product(cartItem.getProduct())
                    .bundle(cartItem.getBundle())
                    .priceSnapshot(price)
                    .quantity(cartItem.getQuantity())
                    .build();
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // Apply discount
        if (request.getDiscountCode() != null && !request.getDiscountCode().isEmpty()) {
            DiscountEntity discount = discountRepository.findByCodeAndIsActiveTrue(request.getDiscountCode())
                    .orElseThrow(() -> new InvalidDataException("Discount code not found or expired"));

            LocalDateTime now = LocalDateTime.now();
            if (discount.getStartDate() != null && now.isBefore(discount.getStartDate())) {
                throw new InvalidDataException("Discount code is not yet active");
            }
            if (discount.getEndDate() != null && now.isAfter(discount.getEndDate())) {
                throw new InvalidDataException("Discount code has expired");
            }

            totalAmount = totalAmount.subtract(discount.getDiscountValue());
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                totalAmount = BigDecimal.ZERO;
            }
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        OrderEntity saved = orderRepository.save(order);

        // Clear cart after order
        cart.getCartItems().clear();
        cartRepository.save(cart);

        log.info("Order created: {} for user: {}", saved.getId(), user.getUsername());
        return toResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return toResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> getMyOrders(int page, int size) {
        Users user = authenticationUtils.getCurrentUser();
        if (user == null)
            throw new ForBiddenException("User not authenticated");

        Page<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(Math.max(page, 0), size));

        List<OrderResponse> responses = orders.getContent().stream()
                .map(this::toResponse).toList();

        return PageResponse.<OrderResponse>builder()
                .data(responses)
                .pageNo(page)
                .pageSize(size)
                .totalItems(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);
        OrderEntity updated = orderRepository.save(order);

        // Notify via WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                    order.getUser().getUsername(),
                    "/queue/order-status",
                    "Order #" + id + " status updated to " + newStatus);
        } catch (Exception e) {
            log.warn("Failed to send WebSocket notification: {}", e.getMessage());
        }

        return toResponse(updated);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Users user = authenticationUtils.getCurrentUser();
        if (user == null)
            throw new ForBiddenException("User not authenticated");

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForBiddenException("You can only cancel your own orders");
        }

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.WAITING_PAYMENT) {
            throw new InvalidDataException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        OrderEntity updated = orderRepository.save(order);
        return toResponse(updated);
    }

    private OrderResponse toResponse(OrderEntity order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> {
                    String name = "PRODUCT".equals(item.getItemType()) && item.getProduct() != null
                            ? item.getProduct().getName()
                            : (item.getBundle() != null ? item.getBundle().getName() : "Unknown");
                    return OrderItemResponse.builder()
                            .id(item.getId())
                            .itemType(item.getItemType())
                            .itemName(name)
                            .priceSnapshot(item.getPriceSnapshot())
                            .quantity(item.getQuantity())
                            .subtotal(item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .build();
                }).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .vatCompanyName(order.getVatCompanyName())
                .vatTaxCode(order.getVatTaxCode())
                .vatPhone(order.getVatPhone())
                .vatAddress(order.getVatAddress())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
