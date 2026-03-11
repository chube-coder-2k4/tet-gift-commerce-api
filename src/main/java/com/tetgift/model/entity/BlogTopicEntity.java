package com.tetgift.model.entity;

import com.tetgift.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blog_topic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogTopicEntity extends BaseEntity<Long> {

    @Column(nullable = false)
    private String name;
}
