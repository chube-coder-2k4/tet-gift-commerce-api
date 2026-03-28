package com.tetgift.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SystemStatusResponse {
    private String status;
    private String version;
    private String javaVersion;
    private long uptime;
    private String message;
}
