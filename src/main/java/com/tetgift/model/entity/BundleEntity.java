package com.tetgift.model.entity;

import com.tetgift.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bundle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleEntity extends BaseEntity<Long> {

    @Column(nullable = false)
    private String name;

    private BigDecimal price;

    @Column(name = "is_custom")
    @Builder.Default
    private boolean isCustom = false;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BundleProductEntity> bundleProducts = new ArrayList<>();

    private String description;

    @Column(name = "image")
    private String image;

    private BigDecimal totalPrice;

    @Column(name = "stock")
    @Builder.Default
    private Integer stock = 0;

    @Version
    private Long version;
}
