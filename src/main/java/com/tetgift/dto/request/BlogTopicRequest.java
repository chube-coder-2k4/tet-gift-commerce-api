package com.tetgift.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlogTopicRequest {
    @NotBlank(message = "Topic name is required")
    private String name;
}
