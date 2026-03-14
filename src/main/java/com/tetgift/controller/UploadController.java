package com.tetgift.controller;

import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
@Tag(name = "Upload Management", description = "Upload file APIs")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @Operation(summary = "Upload image", description = "Upload image to Cloudinary")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseData<String>> uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Upload successful", cloudinaryService.uploadFile(file)));
    }
}

