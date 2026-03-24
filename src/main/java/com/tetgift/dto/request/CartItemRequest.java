package com.tetgift.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "Item type is required (PRODUCT or BUNDLE)")
    private String itemType; // "PRODUCT" or "BUNDLE"

    private Long productId;
    private Long bundleId;

    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;

    private boolean isCustomCombo = false;
    private String customComboData;
}
