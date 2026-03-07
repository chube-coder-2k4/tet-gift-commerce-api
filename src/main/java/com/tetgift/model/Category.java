package com.tetgift.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity<Long> {
    private String name;
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}
