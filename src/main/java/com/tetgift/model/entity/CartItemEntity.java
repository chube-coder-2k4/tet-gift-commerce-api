package com.tetgift.model.entity;

import com.tetgift.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemEntity extends BaseEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;

    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType; // "PRODUCT" or "BUNDLE"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id")
    private BundleEntity bundle;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "is_custom_combo")
    @Builder.Default
    private boolean isCustomCombo = false;

    @Column(columnDefinition = "TEXT", name = "custom_combo_data")
    private String customComboData;
}
