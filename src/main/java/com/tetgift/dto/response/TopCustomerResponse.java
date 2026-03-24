package com.tetgift.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerResponse {
    private Long id;
    private String fullName;
    private String email;
    private Long totalOrders;
    private BigDecimal totalSpent;
}
