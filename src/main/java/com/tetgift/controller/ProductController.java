package com.tetgift.controller;

import com.tetgift.dto.request.ProductRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register Product (multipart)", description = "Register a new product with image upload")
    public ResponseEntity<ResponseData<Long>> registerProduct(
        @RequestPart("request") @Valid ProductRequest productRequest,
        @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        Long productId;
        try {
            if (image != null && !image.isEmpty()) {
                productId = productService.saveProduct(productRequest, image);
            } else {
                productId = productService.saveProduct(productRequest);
            }
        } catch (java.io.IOException e) {
             throw new RuntimeException("Failed to upload image", e);
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseData<>(
                        HttpStatus.CREATED.value(),
                        "Product registered successfully",
                        productId
                ));
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register Product (JSON)", description = "Register a new product without image")
    public ResponseEntity<ResponseData<Long>> registerProductJson(
        @RequestBody @Valid ProductRequest productRequest
    ) {
        Long productId = productService.saveProduct(productRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseData<>(
                        HttpStatus.CREATED.value(),
                        "Product registered successfully",
                        productId
                ));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get Product by ID", description = "Retrieve product details by its ID")
    public ResponseEntity<ResponseData<ProductResponse>> getProductById(@PathVariable Long id){
        ProductResponse productResponse = productService.findProductById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseData<>(
                        HttpStatus.OK.value(),
                        "Product fetched successfully",
                        productResponse
                ));
    }

    @GetMapping
    @Operation(summary = "Get All Products", description = "Retrieve a paginated list of all products")
    public ResponseEntity<ResponseData<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir){
        PageResponse<ProductResponse> productsPage = productService.getAllProducts(page, size, sortBy, sortDir);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseData<>(
                        HttpStatus.OK.value(),
                        "Products fetched successfully",
                        productsPage
                ));
    }
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Product (multipart)", description = "Update product with image upload")
    public ResponseEntity<ResponseData<Long>> updateProduct(@PathVariable Long id,
                                                            @RequestPart("request") @Valid ProductRequest productRequest,
                                                            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        Long updatedProductId;
        try {
            if (image != null && !image.isEmpty()) {
                updatedProductId = productService.updateProduct(id, productRequest, image);
            } else {
                updatedProductId = productService.updateProduct(id, productRequest);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseData<>(
                        HttpStatus.OK.value(),
                        "Product updated successfully",
                        updatedProductId
                ));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Product (JSON)", description = "Update product without image")
    public ResponseEntity<ResponseData<Long>> updateProductJson(@PathVariable Long id,
                                                                @RequestBody @Valid ProductRequest productRequest
    ) {
        Long updatedProductId = productService.updateProduct(id, productRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseData<>(
                        HttpStatus.OK.value(),
                        "Product updated successfully",
                        updatedProductId
                ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Product", description = "Delete a product by its ID")
    public ResponseEntity<ResponseData<String>> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseData<>(
                        HttpStatus.OK.value(),
                        "Product deleted successfully",
                        "Product with ID " + id + " has been deleted"
                ));
    }
}
