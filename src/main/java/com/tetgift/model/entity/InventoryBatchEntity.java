package com.tetgift.model.entity;

import com.tetgift.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventory_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatchEntity extends BaseEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false)
    private String batchCode;

    @Column(nullable = false)
    private Integer importQuantity;

    @Column(nullable = false)
    private Integer currentQuantity;

    @Column(nullable = false)
    private BigDecimal importPrice;

    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}