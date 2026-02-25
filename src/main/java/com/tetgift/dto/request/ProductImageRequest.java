package com.tetgift.dto.request;

import lombok.Data;

@Data
public class ProductImageRequest {
    private String imageUrl;
    private Boolean isThumbnail;
    private Integer sortOrder;
}
