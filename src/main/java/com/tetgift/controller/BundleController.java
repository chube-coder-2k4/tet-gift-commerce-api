package com.tetgift.controller;

import com.tetgift.dto.request.BundleRequest;
import com.tetgift.dto.response.BundleResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.BundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bundles")
@Tag(name = "Bundle Management", description = "APIs for managing gift bundles")
public class BundleController {
    private final BundleService bundleService;

    @PostMapping
    @Operation(summary = "Create bundle", description = "Create a new gift bundle")
    public ResponseEntity<ResponseData<Long>> createBundle(@RequestBody @Valid BundleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>(HttpStatus.CREATED.value(), "Bundle created successfully",
                        bundleService.createBundle(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bundle by ID", description = "Get bundle details with products")
    public ResponseEntity<ResponseData<BundleResponse>> getBundleById(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Bundle fetched successfully",
                bundleService.getBundleById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all bundles", description = "Get paginated list of bundles")
    public ResponseEntity<ResponseData<PageResponse<BundleResponse>>> getAllBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Bundles fetched successfully",
                bundleService.getAllBundles(page, size, sortBy, sortDir)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update bundle", description = "Update an existing bundle")
    public ResponseEntity<ResponseData<Long>> updateBundle(@PathVariable Long id,
            @RequestBody @Valid BundleRequest request) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Bundle updated successfully",
                bundleService.updateBundle(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bundle", description = "Soft delete a bundle")
    public ResponseEntity<ResponseData<Void>> deleteBundle(@PathVariable Long id) {
        bundleService.deleteBundle(id);
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Bundle deleted successfully", null));
    }
}
