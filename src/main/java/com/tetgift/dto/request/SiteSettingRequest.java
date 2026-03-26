package com.tetgift.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettingRequest {
    private String settingKey;
    private String settingValue;
    private String description;
}
