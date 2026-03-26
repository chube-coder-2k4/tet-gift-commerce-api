package com.tetgift.model.entity;

import com.tetgift.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "home_slide")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeSlideEntity extends BaseEntity<Long> {

    @Column(nullable = false)
    private String image;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String subtitle;

    private String cta;

    private String link;

    @Column(name = "slide_order")
    @Builder.Default
    private Integer slideOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}
