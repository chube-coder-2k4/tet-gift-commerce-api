package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long orderId;

    // VAT company info
    private String companyName;
    private String taxCode;
    private String companyPhone;
    private String companyAddress;

    // Financial
    private BigDecimal subtotal;
    private BigDecimal tierDiscountAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    // PDF
    private String pdfUrl;

    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;
}
