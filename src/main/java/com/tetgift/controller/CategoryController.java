package com.tetgift.controller;

import com.tetgift.dto.request.CategoryRequest;
import com.tetgift.dto.response.CategoryResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@Validated
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing categories")
public class CategoryController {
        private final CategoryService cateService;

        @PostMapping
        @Operation(summary = "Create a new category", description = "Create a new category with the provided details")
        public ResponseEntity<ResponseData<CategoryResponse>> createCate(
                        @RequestBody @Valid CategoryRequest cateRequest) {
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(new ResponseData<>(
                                                HttpStatus.CREATED.value(),
                                                "Category created successfully",
                                                cateService.createCate(cateRequest)));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update an existing category", description = "Update the details of an existing category")
        public ResponseEntity<ResponseData<CategoryResponse>> updateCate(@PathVariable Long id,
                        @RequestBody @Valid CategoryRequest cateRequest) {
                return ResponseEntity.ok(new ResponseData<>(
                                HttpStatus.OK.value(),
                                "Category updated successfully",
                                cateService.updateCate(id, cateRequest)));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete a category", description = "Soft delete a category by its ID")
        public ResponseEntity<ResponseData<Void>> deleteCate(@PathVariable Long id) {
                cateService.deleteCate(id);
                return ResponseEntity.ok(new ResponseData<>(
                                HttpStatus.OK.value(),
                                "Category deleted successfully",
                                null));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get category by ID", description = "Retrieve the details of a category by its ID")
        public ResponseEntity<ResponseData<CategoryResponse>> getCateById(@PathVariable Long id) {
                return ResponseEntity.ok(new ResponseData<>(
                                HttpStatus.OK.value(),
                                "Category retrieved successfully",
                                cateService.getCateById(id)));
        }

        @GetMapping
        @Operation(summary = "Get all categories", description = "Retrieve a list of all active categories")
        public ResponseEntity<ResponseData<List<CategoryResponse>>> getAllCates() {
                return ResponseEntity.ok(new ResponseData<>(
                                HttpStatus.OK.value(),
                                "Categories retrieved successfully",
                                cateService.getAllCategory()));
        }
}
