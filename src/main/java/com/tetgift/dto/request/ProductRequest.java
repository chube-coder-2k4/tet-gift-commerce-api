package com.tetgift.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @PositiveOrZero(message = "Stock must be zero or positive")
    private Integer stock = 0;

    private Long categoryId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate manufactureDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expDate;

    private List<ProductImageRequest> images;
}
