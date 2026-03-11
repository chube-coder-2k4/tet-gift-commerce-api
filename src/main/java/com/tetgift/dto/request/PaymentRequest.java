package com.tetgift.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required (COD or VN_PAY)")
    private String method; // "COD" or "VN_PAY"
}
