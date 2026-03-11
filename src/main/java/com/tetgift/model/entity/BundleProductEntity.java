package com.tetgift.model.entity;

import com.tetgift.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bundle_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleProductEntity extends BaseEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private BundleEntity bundle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}
