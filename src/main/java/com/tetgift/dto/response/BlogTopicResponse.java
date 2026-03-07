package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogTopicResponse {
    private Long id;
    private String name;
}
