package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BundleResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private boolean isCustom;
    private boolean isActive;
    private List<BundleProductResponse> products;
}
