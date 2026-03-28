package com.tetgift.model.entity;

import com.tetgift.enums.OrderStatus;
import com.tetgift.model.BaseEntity;
import com.tetgift.model.Users;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity extends BaseEntity<Long> {

    @Column(name = "order_code", unique = true, length = 50)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "total_amount", nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;
    @Column(name = "vat_company_name")
    private String vatCompanyName;

    @Column(name = "vat_tax_code", length = 50)
    private String vatTaxCode;

    @Column(name = "vat_phone", length = 20)
    private String vatPhone;

    @Column(name = "vat_address", length = 500)
    private String vatAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private DiscountEntity discount;

    @Column(name = "discount_code", length = 50)
    private String discountCode;

    @Column(name = "discount_amount")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "refund_bank_name", length = 100)
    private String refundBankName;

    @Column(name = "refund_bank_account", length = 50)
    private String refundBankAccount;

    @Column(name = "refund_account_holder", length = 100)
    private String refundAccountHolder;

    @Column(name = "refund_confirmed_at")
    private LocalDateTime refundConfirmedAt;

    @Column(name = "tier_discount_percent")
    @Builder.Default
    private Integer tierDiscountPercent = 0;

    @Column(name = "tier_discount_amount")
    @Builder.Default
    private BigDecimal tierDiscountAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @PrePersist
    public void generateOrderCode() {
        if (this.orderCode == null) {
            int randomNum = 1000 + (int) (Math.random() * 9000);
            this.orderCode = "SBA-" + System.currentTimeMillis() + "-" + randomNum;
        }
    }
}
