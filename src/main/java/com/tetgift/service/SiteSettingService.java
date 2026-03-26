package com.tetgift.service;

import com.tetgift.dto.request.SiteSettingRequest;
import com.tetgift.dto.response.SiteSettingResponse;

import java.util.List;

public interface SiteSettingService {
    List<SiteSettingResponse> getAllSettings();
    SiteSettingResponse getSettingByKey(String key);
    SiteSettingResponse updateOrCreateSetting(String key, SiteSettingRequest request);
}
