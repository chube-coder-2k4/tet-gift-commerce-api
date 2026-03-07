package com.tetgift.model.entity;

import com.tetgift.enums.OrderStatus;
import com.tetgift.model.BaseEntity;
import com.tetgift.model.Users;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(name = "vat_company_name")
    private String vatCompanyName;

    @Column(name = "vat_tax_code", length = 50)
    private String vatTaxCode;

    @Column(name = "vat_phone", length = 20)
    private String vatPhone;

    @Column(name = "vat_address", length = 500)
    private String vatAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> orderItems = new ArrayList<>();
}
