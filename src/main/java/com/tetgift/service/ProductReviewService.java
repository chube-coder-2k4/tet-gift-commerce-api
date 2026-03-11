package com.tetgift.service;

import com.tetgift.dto.request.ProductReviewRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductReviewResponse;

public interface ProductReviewService {
    ProductReviewResponse createReview(Long productId, ProductReviewRequest request);

    PageResponse<ProductReviewResponse> getReviewsByProductId(Long productId, int page, int size);

    ProductReviewResponse updateReview(Long reviewId, ProductReviewRequest request);

    void deleteReview(Long reviewId);
}
