package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private String vatCompanyName;
    private String vatTaxCode;
    private String vatPhone;
    private String vatAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
