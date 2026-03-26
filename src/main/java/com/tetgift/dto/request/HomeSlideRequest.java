package com.tetgift.dto.request;

import lombok.Data;

@Data
public class HomeSlideRequest {
    private String image;
    private String title;
    private String subtitle;
    private String cta;
    private String link;
    private Integer slideOrder;
    private Boolean isActive;
}
