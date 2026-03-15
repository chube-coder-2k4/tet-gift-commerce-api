package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BlogResponse {
    private Long id;
    private String title;
    private String content;
    private String image;
    private String topicName;
    private Long topicId;
    private LocalDateTime createdAt;
}
