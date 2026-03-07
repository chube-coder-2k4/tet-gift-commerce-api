package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String method;
    private String status;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime paidAt;
    private String paymentUrl; // VNPay redirect URL (only for VN_PAY method)
}
