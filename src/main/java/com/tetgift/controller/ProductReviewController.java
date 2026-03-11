package com.tetgift.controller;

import com.tetgift.dto.request.ProductReviewRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductReviewResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Product Review", description = "APIs for managing product reviews")
public class ProductReviewController {
    private final ProductReviewService reviewService;

    @PostMapping("/products/{productId}/reviews")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create review for a product")
    public ResponseEntity<ResponseData<ProductReviewResponse>> createReview(
            @PathVariable Long productId,
            @RequestBody @Valid ProductReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>(HttpStatus.CREATED.value(), "Review created",
                        reviewService.createReview(productId, request)));
    }

    @GetMapping("/products/{productId}/reviews")
    @Operation(summary = "Get reviews for a product (PUBLIC)")
    public ResponseEntity<ResponseData<PageResponse<ProductReviewResponse>>> getReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Reviews fetched",
                reviewService.getReviewsByProductId(productId, page, size)));
    }

    @PutMapping("/reviews/{reviewId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update own review")
    public ResponseEntity<ResponseData<ProductReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid ProductReviewRequest request) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Review updated",
                reviewService.updateReview(reviewId, request)));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete own review")
    public ResponseEntity<ResponseData<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Review deleted", null));
    }
}
