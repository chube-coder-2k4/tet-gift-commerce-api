package com.tetgift.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String image;          // legacy field (primary image URL)
    private String primaryImage;   // primary image URL for list view
    private String categoryName;
    private Long categoryId;
    private boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate manufactureDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expDate;

    private List<ProductImageResponse> images;  // all images for detail view
}
