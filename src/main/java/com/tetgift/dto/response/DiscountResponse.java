package com.tetgift.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DiscountResponse {
    private Long id;
    private String code;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private Integer usageLimit;
    private Integer usageCount;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    private boolean isActive;

    private String discountType;
    private BigDecimal maxDiscountAmount;
    private BigDecimal ActualDiscountAmount;
    private String getDisplayValue;
    private String typeLable;

}
