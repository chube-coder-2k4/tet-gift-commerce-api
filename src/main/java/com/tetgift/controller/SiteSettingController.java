package com.tetgift.controller;

import com.tetgift.dto.request.SiteSettingRequest;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.dto.response.SiteSettingResponse;
import com.tetgift.service.SiteSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/settings")
@Validated
@Slf4j
@Tag(name = "Site Setting Management", description = "APIs for managing site-wide settings")
public class SiteSettingController {
    private final SiteSettingService settingService;

    @GetMapping
    @Operation(summary = "Get all settings", description = "Retrieve a list of all site-wide settings")
    public ResponseEntity<ResponseData<List<SiteSettingResponse>>> getAllSettings() {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Settings retrieved successfully",
                settingService.getAllSettings()));
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get setting by key", description = "Retrieve a specific setting by its unique key")
    public ResponseEntity<ResponseData<SiteSettingResponse>> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Setting retrieved successfully",
                settingService.getSettingByKey(key)));
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update or create setting", description = "Update an existing setting or create a new one if it doesn't exist")
    public ResponseEntity<ResponseData<SiteSettingResponse>> updateSetting(
            @PathVariable String key,
            @RequestBody @Valid SiteSettingRequest request) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Setting updated successfully",
                settingService.updateOrCreateSetting(key, request)));
    }
}
