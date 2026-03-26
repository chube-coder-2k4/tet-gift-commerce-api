package com.tetgift.service.impl;

import com.tetgift.dto.request.SiteSettingRequest;
import com.tetgift.dto.response.SiteSettingResponse;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.SiteSetting;
import com.tetgift.repository.jpa.SiteSettingRepository;
import com.tetgift.service.SiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteSettingServiceImpl implements SiteSettingService {
    private final SiteSettingRepository repository;

    @Override
    public List<SiteSettingResponse> getAllSettings() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SiteSettingResponse getSettingByKey(String key) {
        SiteSetting setting = repository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));
        return toResponse(setting);
    }

    @Override
    @Transactional
    public SiteSettingResponse updateOrCreateSetting(String key, SiteSettingRequest request) {
        SiteSetting setting = repository.findBySettingKey(key)
                .orElse(SiteSetting.builder()
                        .settingKey(key)
                        .build());
        
        setting.setSettingValue(request.getSettingValue());
        if (request.getDescription() != null) {
            setting.setDescription(request.getDescription());
        }
        
        SiteSetting saved = repository.save(setting);
        return toResponse(saved);
    }

    private SiteSettingResponse toResponse(SiteSetting setting) {
        return SiteSettingResponse.builder()
                .id(setting.getId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .description(setting.getDescription())
                .build();
    }
}
