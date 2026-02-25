package com.tetgift.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;


@Data
public class ProductRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    private BigDecimal originalPrice;
    private ProductInventoryRequest inventory;
    private List<ProductImageRequest> images;
    private Set<Long> badgeIds;
}
