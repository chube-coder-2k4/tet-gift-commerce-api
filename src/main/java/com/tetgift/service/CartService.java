package com.tetgift.service;

import com.tetgift.dto.request.CartItemRequest;
import com.tetgift.dto.response.CartResponse;

public interface CartService {
    CartResponse getMyCart();

    CartResponse addItemToCart(CartItemRequest request);

    CartResponse updateCartItem(Long itemId, Integer quantity);

    void removeCartItem(Long itemId);

    void clearCart();
}
