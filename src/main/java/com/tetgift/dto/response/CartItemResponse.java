package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private String itemType;
    private Long itemId;
    private String itemName;
    private String itemImage;
    private BigDecimal itemPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Boolean isCustomCombo;
    private String customComboData;
}
