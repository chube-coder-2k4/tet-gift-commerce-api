package com.tetgift.model.entity;

import com.tetgift.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceEntity extends BaseEntity<Long> {

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    // VAT info (snapshot from order)
    @Column(name = "company_name")
    private String companyName;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "company_phone", length = 20)
    private String companyPhone;

    @Column(name = "company_address", length = 500)
    private String companyAddress;

    // Financial
    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tier_discount_amount")
    @Builder.Default
    private BigDecimal tierDiscountAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    // PDF storage
    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "pdf_public_id", length = 200)
    private String pdfPublicId;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
}
