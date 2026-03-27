package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryBatchResponse {
    private Long id;
    private String batchCode;
    private Long productId;
    private String productName;
    private Integer importQuantity;
    private Integer currentQuantity;
    private BigDecimal importPrice;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;
}
