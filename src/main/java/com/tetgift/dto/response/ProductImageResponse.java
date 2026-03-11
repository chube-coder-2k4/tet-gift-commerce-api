package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    private String imageType;
    private String publicId;
    private boolean isPrimary;
}
