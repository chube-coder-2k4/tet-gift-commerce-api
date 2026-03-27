package com.tetgift.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSettingResponse {
    private Long id;
    private String settingKey;
    private String settingValue;
    private String description;
}
