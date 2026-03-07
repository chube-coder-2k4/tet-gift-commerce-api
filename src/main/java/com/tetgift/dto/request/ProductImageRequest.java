package com.tetgift.dto.request;

import lombok.Data;

@Data
public class ProductImageRequest {
    private String imageUrl;
    private String imageType;
    private String publicId;
    private boolean isPrimary;
}
