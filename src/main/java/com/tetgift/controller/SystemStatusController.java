package com.tetgift.controller;

import com.tetgift.dto.response.ResponseData;
import com.tetgift.dto.response.SystemStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/status")
@Slf4j
@Tag(name = "System Status", description = "Simple health check and system information endpoint")
public class SystemStatusController {

    @GetMapping
    @Operation(summary = "Get system status", description = "Returns basic information about the system health and uptime")
    public ResponseEntity<ResponseData<SystemStatusResponse>> getStatus() {
        SystemStatusResponse status = SystemStatusResponse.builder()
                .status("UP")
                .version("1.0.0")
                .javaVersion(System.getProperty("java.version"))
                .uptime(ManagementFactory.getRuntimeMXBean().getUptime())
                .message("Tet Gift Commerce API is running smoothly!")
                .build();

        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "System status retrieved successfully",
                status));
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping the server", description = "Returns 'pong' if the server is responsive")
    public ResponseEntity<ResponseData<String>> ping() {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Success",
                "pong"));
    }
}
