package com.tetgift.model.entity;

import com.tetgift.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity extends BaseEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id")
    private BundleEntity bundle;

    @Column(name = "price_snapshot", nullable = false)
    private BigDecimal priceSnapshot;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "is_custom_combo")
    @Builder.Default
    private Boolean isCustomCombo = false;

    @Column(columnDefinition = "TEXT", name = "custom_combo_data")
    private String customComboData;
}
