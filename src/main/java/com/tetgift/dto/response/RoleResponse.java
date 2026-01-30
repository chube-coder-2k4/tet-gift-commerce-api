package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
}
