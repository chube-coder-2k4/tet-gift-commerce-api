package com.tetgift.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @Positive(message = "Minimum order amount must be positive")
    private BigDecimal minOrderAmount;

    @Positive(message = "Usage limit must be positive")
    private Integer usageLimit;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
}
