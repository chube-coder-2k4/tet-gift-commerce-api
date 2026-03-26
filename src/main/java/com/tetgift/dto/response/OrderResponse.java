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

    // Customer info
    private String customerName;
    private String customerEmail;

    // Delivery info
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;

    // Discount info
    private String discountCode;
    private BigDecimal discountAmount;

    // VAT info
    private String vatCompanyName;
    private String vatTaxCode;
    private String vatPhone;
    private String vatAddress;

    // Refund info
    private String refundBankName;
    private String refundBankAccount;
    private String refundAccountHolder;
    private LocalDateTime refundConfirmedAt;

    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
