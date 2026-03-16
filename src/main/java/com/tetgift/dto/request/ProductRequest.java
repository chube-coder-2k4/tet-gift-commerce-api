package com.tetgift.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must be less than 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Min(value = 1000, message = "Price must be at least 1,000 VND")
    private BigDecimal price;

    @PositiveOrZero(message = "Stock must be zero or positive")
    private Integer stock = 0;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate manufactureDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expDate;

    private List<ProductImageRequest> images;
}
