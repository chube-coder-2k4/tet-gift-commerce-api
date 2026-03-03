package com.tetgift.controller;

import com.tetgift.dto.request.CategoryRequest;
import com.tetgift.dto.request.RoleRequest;
import com.tetgift.dto.response.CategoryResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.dto.response.RoleResponse;
import com.tetgift.service.CategoryService;
import com.tetgift.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cate")
@Validated
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing categories")
public class CategoryController {
    private final CategoryService cateService;

    @PostMapping
    @Operation(summary = "Create a new cate", description = "Create a new cate with the provided details")
    public ResponseEntity<ResponseData<CategoryResponse>> createCate(@RequestBody @Valid CategoryRequest cateRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED.value())
                .body(new ResponseData<CategoryResponse>(
                        HttpStatus.CREATED.value(),
                        "Cate created successfully",
                        cateService.createCate(cateRequest)
                        ));
    }

    @PutMapping
    @Operation(summary = "Update an existing cate", description = "Update the details of an existing cate")
    public ResponseEntity<ResponseData<CategoryResponse>> updateCate(@RequestParam Long id, @RequestBody @Valid CategoryRequest cateRequest) {
        return ResponseEntity
                .status(HttpStatus.OK.value())
                .body(new ResponseData<CategoryResponse>(
                        HttpStatus.OK.value(),
                        "Category updated successfully",
                        cateService.updateCate(id, cateRequest)
                ));
    }

    @DeleteMapping
    @Operation(summary = "Delete a cate", description = "Delete a cate by its ID")
    public ResponseEntity<ResponseData<Void>> deleteCate(@RequestParam Long id) {
        cateService.deleteCate(id);
        return ResponseEntity
                .status(HttpStatus.OK.value())
                .body(new ResponseData<Void>(
                        HttpStatus.OK.value(),
                        "Cate deleted successfully",
                        null
                ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cate by ID", description = "Retrieve the details of a cate by its ID")
    public ResponseEntity<ResponseData<CategoryResponse>> getCateById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK.value())
                .body(new ResponseData<CategoryResponse>(
                        HttpStatus.OK.value(),
                        "Cate retrieved successfully",
                        cateService.getCateById(id)
                ));
    }

    @GetMapping
    @Operation(summary = "Get all cates", description = "Retrieve a list of all cates")
    public ResponseEntity<ResponseData<Iterable<CategoryResponse>>> getAllCates() {
        return ResponseEntity
                .status(HttpStatus.OK.value())
                .body(new ResponseData<Iterable<CategoryResponse>>(
                        HttpStatus.OK.value(),
                        "Cates retrieved successfully",
                        cateService.getAllCategory()
                ));
    }

}
