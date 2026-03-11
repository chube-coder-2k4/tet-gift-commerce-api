package com.tetgift.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlogRequest {
    @NotBlank(message = "Blog title is required")
    private String title;
    private String content;
    private Long topicId;
}
