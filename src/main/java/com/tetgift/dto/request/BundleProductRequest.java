package com.tetgift.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BundleProductRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;
}
