package com.tetgift.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BundleRequest {
    @NotBlank(message = "Bundle name is required")
    private String name;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private boolean isCustom = false;

    private List<BundleProductRequest> products;
}
