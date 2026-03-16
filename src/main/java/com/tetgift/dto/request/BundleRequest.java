package com.tetgift.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BundleRequest {
    @NotBlank(message = "Bundle name is required")
    @Size(max = 255, message = "Bundle name must be less than 255 characters")
    private String name;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @JsonProperty("isCustom")
    private boolean isCustom = false;

    @Size(min = 1, message = "Bundle must contain at least one product")
    private List<BundleProductRequest> products;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;
}
