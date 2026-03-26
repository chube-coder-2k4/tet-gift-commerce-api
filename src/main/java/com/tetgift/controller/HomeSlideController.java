package com.tetgift.controller;

import com.tetgift.dto.request.HomeSlideRequest;
import com.tetgift.dto.response.HomeSlideResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.HomeSlideService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/slides")
@RequiredArgsConstructor
public class HomeSlideController {

    private final HomeSlideService homeSlideService;

    @GetMapping
    @Operation(summary = "Get All Active Slides", description = "Retrieve a list of all active slides ordered by slide order.")
    public ResponseEntity<ResponseData<List<HomeSlideResponse>>> getActiveSlides() {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Active slides fetched successfully",
                homeSlideService.getAllActiveSlides()
        ));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Slides (Admin)", description = "Retrieve a list of all slides (including inactive) for administrative purposes.")
    public ResponseEntity<ResponseData<List<HomeSlideResponse>>> getAllSlidesAdmin() {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "All slides fetched successfully",
                homeSlideService.getAllSlidesAdmin()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Slide by ID")
    public ResponseEntity<ResponseData<HomeSlideResponse>> getSlideById(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Slide fetched successfully",
                homeSlideService.getSlideById(id)
        ));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Slide")
    public ResponseEntity<ResponseData<HomeSlideResponse>> createSlide(@RequestBody @Valid HomeSlideRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseData<>(
                HttpStatus.CREATED.value(),
                "Slide created successfully",
                homeSlideService.createSlide(request)
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Slide")
    public ResponseEntity<ResponseData<HomeSlideResponse>> updateSlide(@PathVariable Long id, @RequestBody @Valid HomeSlideRequest request) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Slide updated successfully",
                homeSlideService.updateSlide(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Slide")
    public ResponseEntity<ResponseData<Void>> deleteSlide(@PathVariable Long id) {
        homeSlideService.deleteSlide(id);
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Slide deleted successfully",
                null
        ));
    }
}
