package com.tetgift.service.impl;

import com.tetgift.dto.request.CartItemRequest;
import com.tetgift.dto.response.CartItemResponse;
import com.tetgift.dto.response.CartResponse;
import com.tetgift.exception.ForBiddenException;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.Users;
import com.tetgift.model.entity.*;
import com.tetgift.repository.jpa.BundleRepository;
import com.tetgift.repository.jpa.CartItemRepository;
import com.tetgift.repository.jpa.CartRepository;
import com.tetgift.repository.jpa.ProductRepository;
import com.tetgift.service.CartService;
import com.tetgift.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final BundleRepository bundleRepository;
    private final AuthenticationUtils authenticationUtils;

    @Override
    public CartResponse getMyCart() {
        CartEntity cart = getOrCreateCart();
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(CartItemRequest request) {
        CartEntity cart = getOrCreateCart();

        CartItemEntity item = CartItemEntity.builder()
                .cart(cart)
                .itemType(request.getItemType().toUpperCase())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .build();

        if ("PRODUCT".equalsIgnoreCase(request.getItemType())) {
            if (request.getProductId() == null) {
                throw new InvalidDataException("Product ID is required for PRODUCT item type");
            }
            ProductEntity product = productRepository.findByIdAndIsActiveTrue(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));
            item.setProduct(product);
        } else if ("BUNDLE".equalsIgnoreCase(request.getItemType())) {
            if (request.getBundleId() == null) {
                throw new InvalidDataException("Bundle ID is required for BUNDLE item type");
            }
            BundleEntity bundle = bundleRepository.findByIdAndIsActiveTrue(request.getBundleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bundle not found: " + request.getBundleId()));
            item.setBundle(bundle);
        } else {
            throw new InvalidDataException("Invalid item type. Must be PRODUCT or BUNDLE");
        }

        cart.getCartItems().add(item);
        CartEntity saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long itemId, Integer quantity) {
        CartEntity cart = getOrCreateCart();
        CartItemEntity item = cart.getCartItems().stream()
                .filter(ci -> ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));

        if (quantity <= 0) {
            cart.getCartItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        CartEntity saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void removeCartItem(Long itemId) {
        CartEntity cart = getOrCreateCart();
        cart.getCartItems().removeIf(ci -> ci.getId().equals(itemId));
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart() {
        CartEntity cart = getOrCreateCart();
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    private CartEntity getOrCreateCart() {
        Users user = authenticationUtils.getCurrentUser();
        if (user == null) {
            throw new ForBiddenException("User not authenticated");
        }

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    CartEntity newCart = CartEntity.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse toResponse(CartEntity cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(this::toItemResponse).toList();

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalPrice(totalPrice)
                .totalItems(items.size())
                .build();
    }

    private CartItemResponse toItemResponse(CartItemEntity item) {
        String name;
        BigDecimal price;
        Long itemId;

        if ("PRODUCT".equals(item.getItemType()) && item.getProduct() != null) {
            name = item.getProduct().getName();
            price = item.getProduct().getPrice();
            itemId = item.getProduct().getId();
        } else if ("BUNDLE".equals(item.getItemType()) && item.getBundle() != null) {
            name = item.getBundle().getName();
            price = item.getBundle().getPrice();
            itemId = item.getBundle().getId();
        } else {
            name = "Unknown";
            price = BigDecimal.ZERO;
            itemId = null;
        }

        return CartItemResponse.builder()
                .id(item.getId())
                .itemType(item.getItemType())
                .itemId(itemId)
                .itemName(name)
                .itemPrice(price)
                .quantity(item.getQuantity())
                .subtotal(price.multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}
