package com.tetgift.model.entity;

import com.tetgift.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogEntity extends BaseEntity<Long> {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private BlogTopicEntity topic;
}
