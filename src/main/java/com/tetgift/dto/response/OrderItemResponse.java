package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private String itemType;
    private String itemName;
    private BigDecimal priceSnapshot;
    private Integer quantity;
    private BigDecimal subtotal;
}
