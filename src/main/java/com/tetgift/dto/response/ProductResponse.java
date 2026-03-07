package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String categoryName;
    private Long categoryId;
    private boolean isActive;
    private LocalDate manufactureDate;
    private LocalDate expDate;
    private List<ProductImageResponse> images;
}
