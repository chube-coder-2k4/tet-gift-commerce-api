package com.tetgift.model;

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
}
