package com.tetgift.service.impl;

import com.tetgift.dto.request.OrderRequest;
import com.tetgift.dto.request.RefundRequest;
import com.tetgift.dto.response.OrderItemResponse;
import com.tetgift.dto.response.OrderResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.enums.OrderStatus;
import com.tetgift.exception.ForBiddenException;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.Users;
import com.tetgift.model.Address;
import com.tetgift.model.entity.*;
import com.tetgift.repository.jpa.*;
import com.tetgift.service.OrderService;
import com.tetgift.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final DiscountRepository discountRepository;
    private final AddressRepository addressRepository;
    private final AuthenticationUtils authenticationUtils;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final InventoryBatchRepository batchRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Users user = authenticationUtils.getCurrentUser();
        if (user == null)
            throw new ForBiddenException("User not authenticated");

        CartEntity cart = cartRepository.findWithItemsByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new InvalidDataException("Cart is empty");
        }

        // Lookup delivery address and snapshot
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + request.getAddressId()));

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .status(OrderStatus.CREATED)
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getPhone())
                .shippingAddress(address.getAddressDetail())
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
                ProductEntity product = cartItem.getProduct();
                // THAY THẾ ĐOẠN NÀY:
                // Gọi hàm trừ kho theo lô thay vì trừ trực tiếp vào product.stock
                deductStockFromBatches(product, cartItem.getQuantity());
                price = product.getPrice();
            } else if ("BUNDLE".equals(cartItem.getItemType()) && cartItem.getBundle() != null) {
                BundleEntity bundle = cartItem.getBundle();
                for (BundleProductEntity bundleProduct : bundle.getBundleProducts()) {
                    ProductEntity componentProduct = bundleProduct.getProduct();
                    int totalNeeded = cartItem.getQuantity() * bundleProduct.getQuantity();
                    // THAY THẾ ĐOẠN NÀY:
                    // Trừ kho theo lô cho từng sản phẩm thành phần trong Bundle
                    deductStockFromBatches(componentProduct, totalNeeded);
                }
                if (Boolean.TRUE.equals(cartItem.getIsCustomCombo()) && cartItem.getCustomComboData() != null) {
                    try {
                        JsonNode comboData = objectMapper.readTree(cartItem.getCustomComboData());
                        price = comboData.has("totalPrice") ? new BigDecimal(comboData.get("totalPrice").asText()) : bundle.getPrice();
                    } catch (Exception e) {
                        log.error("Lỗi parse customComboData", e);
                        price = bundle.getPrice();
                    }
                } else {
                    price = bundle.getPrice();
                }
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
                    .isCustomCombo(cartItem.getIsCustomCombo())
                    .customComboData(cartItem.getCustomComboData())
                    .build();
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // ---- Subtotal (before any discounts) ----
        BigDecimal subtotalBeforeDiscount = totalAmount;

        // ---- Apply Tier Discount (automatic, based on order total) ----
        int tierPercent = calculateTierDiscountPercent(totalAmount);
        BigDecimal tierDiscountAmount = BigDecimal.ZERO;
        if (tierPercent > 0) {
            tierDiscountAmount = totalAmount
                    .multiply(BigDecimal.valueOf(tierPercent))
                    .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.FLOOR);
            totalAmount = totalAmount.subtract(tierDiscountAmount);
            order.setTierDiscountPercent(tierPercent);
            order.setTierDiscountAmount(tierDiscountAmount);
            log.info("Applied tier discount: {}% = {} VND (subtotal: {})", tierPercent, tierDiscountAmount, subtotalBeforeDiscount);
        }

        // ---- Apply Discount Code (manual, user entered) ----
        if (request.getDiscountCode() != null && !request.getDiscountCode().isEmpty()) {
            DiscountEntity discount = discountRepository
                    .findByCodeAndIsActiveTrue(request.getDiscountCode().toUpperCase())
                    .orElseThrow(() -> new InvalidDataException("Discount code not found or expired"));

            LocalDateTime now = LocalDateTime.now();
            if (discount.getStartDate() != null && now.isBefore(discount.getStartDate())) {
                throw new InvalidDataException("Discount code is not yet active");
            }
            if (discount.getEndDate() != null && now.isAfter(discount.getEndDate())) {
                throw new InvalidDataException("Discount code has expired");
            }
            if (discount.getUsageLimit() != null && discount.getUsageCount() >= discount.getUsageLimit()) {
                throw new InvalidDataException("Discount code has reached its usage limit");
            }
            // Min order check is against subtotal BEFORE tier discount
            if (discount.getMinOrderAmount() != null && subtotalBeforeDiscount.compareTo(discount.getMinOrderAmount()) < 0) {
                throw new InvalidDataException("Order total must be at least " + discount.getMinOrderAmount()
                        + " VND to use this discount code");
            }

            BigDecimal discountAmount = discount.getDiscountValue();
            if (discountAmount.compareTo(totalAmount) > 0) {
                discountAmount = totalAmount;
            }

            totalAmount = totalAmount.subtract(discountAmount);

            // Link discount to order
            order.setDiscount(discount);
            order.setDiscountCode(discount.getCode());
            order.setDiscountAmount(discountAmount);

            // Increment usage count
            discount.setUsageCount(discount.getUsageCount() + 1);
            discountRepository.save(discount);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        OrderEntity saved = orderRepository.save(order);

        // Cart is NOT cleared here - it will be cleared:
        // - For COD: immediately when payment is created
        // - For VN_PAY: only after payment success callback
        // This prevents losing cart data if VNPay payment fails

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
    public OrderResponse getOrderByCode(String orderCode) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + orderCode));
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
    public PageResponse<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderEntity> orderPage = orderRepository.findAll(pageable);

        List<OrderResponse> responses = orderPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<OrderResponse>builder()
                .data(responses)
                .pageNo(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalItems(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
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

    @Override
    public void deductStockFromBatches(ProductEntity product, int neededQuantity) {
        // Tìm các lô hàng còn hạn, sắp xếp theo ngày hết hạn sớm nhất
        List<InventoryBatchEntity> activeBatches = batchRepository
                .findByProductIdAndCurrentQuantityGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
                        product.getId(), 0, LocalDate.now());

        int remainingToDeduct = neededQuantity;

        for (InventoryBatchEntity batch : activeBatches) {
            if (remainingToDeduct <= 0) break;

            int availableInBatch = batch.getCurrentQuantity();
            if (availableInBatch >= remainingToDeduct) {
                batch.setCurrentQuantity(availableInBatch - remainingToDeduct);
                remainingToDeduct = 0;
            } else {
                remainingToDeduct -= availableInBatch;
                batch.setCurrentQuantity(0);
            }
            batchRepository.save(batch);
        }

        if (remainingToDeduct > 0) {
            throw new InvalidDataException("Sản phẩm " + product.getName() + " không đủ hàng trong các lô khả dụng.");
        }

        // Cập nhật lại cột stock tổng ở Product để đồng bộ (optional)
        product.setStock(product.getStock() - neededQuantity);
        productRepository.save(product);
    }

    @Transactional
    public OrderResponse cancelOrderWithRefund(Long orderId, RefundRequest request) {
        Users user = authenticationUtils.getCurrentUser();
        if (user == null)
            throw new ForBiddenException("User not authenticated");

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForBiddenException("You can only cancel your own orders");
        }

        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidDataException(
                    "Only paid orders can be cancelled with refund. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED_PENDING_REFUND);
        order.setRefundBankName(request.getBankName());
        order.setRefundBankAccount(request.getBankAccount());
        order.setRefundAccountHolder(request.getAccountHolder());

        OrderEntity updated = orderRepository.save(order);

        // Notify via WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                    order.getUser().getUsername(),
                    "/queue/order-status",
                    "Order #" + orderId + " has been cancelled. Refund is pending.");
        } catch (Exception e) {
            log.warn("Failed to send WebSocket notification: {}", e.getMessage());
        }

        log.info("Order {} cancelled with refund request by user {}", orderId, user.getUsername());
        return toResponse(updated);
    }


    //    public PageResponse<OrderResponse> getRefundOrders(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<OrderEntity> orderPage = orderRepository.findByStatus(OrderStatus.CANCELLED_PENDING_REFUND, pageable);
//
//        List<OrderResponse> responses = orderPage.getContent().stream()
//                .map(this::toResponse)
//                .toList();
//
//        return PageResponse.<OrderResponse>builder()
//                .data(responses)
//                .pageNo(orderPage.getNumber())
//                .pageSize(orderPage.getSize())
//                .totalItems(orderPage.getTotalElements())
//                .totalPages(orderPage.getTotalPages())
//                .build();
//    }
    @Override
    public PageResponse<OrderResponse> getRefundOrders(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 1. Xử lý điều kiện Filter trạng thái
        List<OrderStatus> targetStatuses;
        if (status != null && !status.trim().isEmpty()) {
            // Lọc theo đúng trạng thái truyền vào (Ví dụ: truyền vào "CANCELLED_PENDING_REFUND")
            targetStatuses = List.of(OrderStatus.valueOf(status.toUpperCase()));
        } else {
            // Nếu không truyền filter -> Lấy CẢ HAI: Đang chờ hoàn và Đã hoàn tiền
            targetStatuses = List.of(
                    OrderStatus.CANCELLED_PENDING_REFUND,
                    OrderStatus.CANCELLED_REFUNDED
            );
        }

        // 2. Gọi Repository đã viết ở Bước 1
        Page<OrderEntity> orderPage = orderRepository.findRefundOrders(targetStatuses, keyword, pageable);

        // 3. Map sang Response
        List<OrderResponse> responses = orderPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<OrderResponse>builder()
                .data(responses)
                .pageNo(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalItems(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public OrderResponse confirmRefund(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.CANCELLED_PENDING_REFUND) {
            throw new InvalidDataException(
                    "Order is not in pending refund status. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED_REFUNDED);
        order.setRefundConfirmedAt(LocalDateTime.now());
        OrderEntity updated = orderRepository.save(order);

        // Notify customer via WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                    order.getUser().getUsername(),
                    "/queue/order-status",
                    "Order #" + orderId + " refund has been completed.");
        } catch (Exception e) {
            log.warn("Failed to send WebSocket notification: {}", e.getMessage());
        }

        log.info("Refund confirmed for order {}", orderId);
        return toResponse(updated);
    }

    @Override
    public byte[] exportRefundOrders(LocalDateTime startDateTime, LocalDateTime endDateTime, String status, String format) {
        List<OrderStatus> targetStatuses;
        if (status != null && !status.trim().isEmpty()) {
            targetStatuses = List.of(OrderStatus.valueOf(status.toUpperCase()));
        } else {
            targetStatuses = List.of(
                    OrderStatus.CANCELLED_PENDING_REFUND,
                    OrderStatus.CANCELLED_REFUNDED
            );
        }

        List<OrderEntity> orders = orderRepository.findByStatusInAndCreatedAtBetween(
                targetStatuses, startDateTime, endDateTime);

        if ("csv".equalsIgnoreCase(format)) {
            return generateCsv(orders);
        } else {
            return generateExcel(orders);
        }
    }

    private byte[] generateExcel(List<OrderEntity> orders) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Refund Orders");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {
                    "Order ID", "Customer Name", "Customer Email",
                    "Customer Phone", "Total Amount", "Order Date",
                    "Bank Name", "Bank Account", "Account Holder", "Status"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            int rowNum = 1;
            for (OrderEntity order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getUser().getFullName() != null ? order.getUser().getFullName() : "");
                row.createCell(2).setCellValue(order.getUser().getEmail() != null ? order.getUser().getEmail() : "");
                row.createCell(3).setCellValue(order.getUser().getPhone() != null ? order.getUser().getPhone() : "");
                row.createCell(4).setCellValue(order.getTotalAmount().doubleValue());
                row.createCell(5).setCellValue(order.getCreatedAt().format(formatter));
                row.createCell(6).setCellValue(order.getRefundBankName() != null ? order.getRefundBankName() : "");
                row.createCell(7).setCellValue(order.getRefundBankAccount() != null ? order.getRefundBankAccount() : "");
                row.createCell(8).setCellValue(order.getRefundAccountHolder() != null ? order.getRefundAccountHolder() : "");
                row.createCell(9).setCellValue(translateStatus(order.getStatus()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Lỗi khi xuất file Excel", e);
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private byte[] generateCsv(List<OrderEntity> orders) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            writer.println("Order ID,Customer Name,Customer Email,Customer Phone,Total Amount,Order Date,Bank Name,Bank Account,Account Holder,Status");

            for (OrderEntity order : orders) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        order.getId(),
                        escapeCsv(order.getUser().getFullName()),
                        escapeCsv(order.getUser().getEmail()),
                        escapeCsv(order.getUser().getPhone()),
                        order.getTotalAmount().toPlainString(),
                        order.getCreatedAt().format(formatter),
                        escapeCsv(order.getRefundBankName()),
                        escapeCsv(order.getRefundBankAccount()),
                        escapeCsv(order.getRefundAccountHolder()),
                        escapeCsv(translateStatus(order.getStatus())));
            }
            writer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Lỗi khi xuất file CSV", e);
            throw new RuntimeException("Failed to generate CSV file", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private String translateStatus(OrderStatus status) {
        if (status == OrderStatus.CANCELLED_PENDING_REFUND) return "Chờ hoàn tiền";
        if (status == OrderStatus.CANCELLED_REFUNDED) return "Đã hoàn tiền";
        return status.name();
    }

    private OrderResponse toResponse(OrderEntity order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> {
                    String name = "Unknown";
                    if ("PRODUCT".equals(item.getItemType()) && item.getProduct() != null) {
                        name = item.getProduct().getName();
                    } else if ("BUNDLE".equals(item.getItemType()) && item.getBundle() != null) {
                        if (Boolean.TRUE.equals(item.getIsCustomCombo()) && item.getCustomComboData() != null) {
                            try {
                                JsonNode comboData = objectMapper.readTree(item.getCustomComboData());
                                name = comboData.has("name") ? comboData.get("name").asText() : "Custom Combo";
                            } catch (Exception e) {
                                name = item.getBundle().getName();
                            }
                        } else {
                            name = item.getBundle().getName();
                        }
                    }

                    return OrderItemResponse.builder()
                            .id(item.getId())
                            .itemType(item.getItemType())
                            .itemName(name)
                            .priceSnapshot(item.getPriceSnapshot())
                            .quantity(item.getQuantity())
                            .subtotal(item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .isCustomCombo(item.getIsCustomCombo())
                            .customComboData(item.getCustomComboData())
                            .build();
                }).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .subtotalBeforeDiscount(calculateSubtotal(order))
                .customerName(order.getUser().getFullName())
                .customerEmail(order.getUser().getEmail())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .discountCode(order.getDiscountCode())
                .discountAmount(order.getDiscountAmount())
                .tierDiscountPercent(order.getTierDiscountPercent())
                .tierDiscountAmount(order.getTierDiscountAmount())
                .vatCompanyName(order.getVatCompanyName())
                .vatTaxCode(order.getVatTaxCode())
                .vatPhone(order.getVatPhone())
                .vatAddress(order.getVatAddress())
                .refundBankName(order.getRefundBankName())
                .refundBankAccount(order.getRefundBankAccount())
                .refundAccountHolder(order.getRefundAccountHolder())
                .refundConfirmedAt(order.getRefundConfirmedAt())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * Calculate tier discount percentage based on order subtotal.
     * >= 50,000,000 -> 10%
     * >= 30,000,000 ->  8%
     * >= 15,000,000 ->  5%
     * >= 10,000,000 ->  3%
     * < 10,000,000  ->  0%
     */
    private int calculateTierDiscountPercent(BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.valueOf(50_000_000)) >= 0) return 10;
        if (subtotal.compareTo(BigDecimal.valueOf(30_000_000)) >= 0) return 8;
        if (subtotal.compareTo(BigDecimal.valueOf(15_000_000)) >= 0) return 5;
        if (subtotal.compareTo(BigDecimal.valueOf(10_000_000)) >= 0) return 3;
        return 0;
    }

    /**
     * Calculate subtotal from order items (before any discounts).
     */
    private BigDecimal calculateSubtotal(OrderEntity order) {
        return order.getOrderItems().stream()
                .map(item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
