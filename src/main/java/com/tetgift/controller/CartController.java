package com.tetgift.controller;

import com.tetgift.dto.request.CartItemRequest;
import com.tetgift.dto.response.CartResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart Management", description = "APIs for managing shopping cart")
public class CartController {
    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get my cart", description = "Get the shopping cart of the current user")
    public ResponseEntity<ResponseData<CartResponse>> getMyCart() {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Cart fetched successfully", cartService.getMyCart()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a product or bundle to the cart")
    public ResponseEntity<ResponseData<CartResponse>> addItem(@RequestBody @Valid CartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>(HttpStatus.CREATED.value(), "Item added to cart",
                        cartService.addItemToCart(request)));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity", description = "Update the quantity of a cart item")
    public ResponseEntity<ResponseData<CartResponse>> updateItem(@PathVariable Long itemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Cart item updated",
                cartService.updateCartItem(itemId, quantity)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove cart item", description = "Remove an item from the cart")
    public ResponseEntity<ResponseData<Void>> removeItem(@PathVariable Long itemId) {
        cartService.removeCartItem(itemId);
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Item removed from cart", null));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the cart")
    public ResponseEntity<ResponseData<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Cart cleared", null));
    }
}
