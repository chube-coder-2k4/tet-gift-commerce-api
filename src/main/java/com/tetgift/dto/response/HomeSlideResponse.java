package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HomeSlideResponse {
    private Long id;
    private String image;
    private String title;
    private String subtitle;
    private String cta;
    private String link;
    private Integer slideOrder;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
