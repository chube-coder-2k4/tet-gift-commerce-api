package com.tetgift.dto.response;

import lombok.Data;

@Data
public class ProductImageResponse {
    private String imageUrl;
    private Boolean isThumbnail;
    private Integer sortOrder;
}
