package com.tetgift.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountRequest {
    @NotBlank(message = "Discount code is required")
    private String code;

    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
