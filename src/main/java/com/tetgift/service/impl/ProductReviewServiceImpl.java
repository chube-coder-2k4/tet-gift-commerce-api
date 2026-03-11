package com.tetgift.service.impl;

import com.tetgift.dto.request.ProductReviewRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductReviewResponse;
import com.tetgift.exception.ForBiddenException;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ProductNotFoundException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.Users;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.model.entity.ProductReviewEntity;
import com.tetgift.repository.jpa.ProductRepository;
import com.tetgift.repository.jpa.ProductReviewRepository;
import com.tetgift.service.ProductReviewService;
import com.tetgift.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final AuthenticationUtils authenticationUtils;

    @Override
    @Transactional
    public ProductReviewResponse createReview(Long productId, ProductReviewRequest request) {
        Users user = authenticationUtils.getCurrentUser();
        if (user == null)
            throw new ForBiddenException("User not authenticated");

        ProductEntity product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new InvalidDataException("You have already reviewed this product");
        }

        ProductReviewEntity review = ProductReviewEntity.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    @Override
    public PageResponse<ProductReviewResponse> getReviewsByProductId(Long productId, int page, int size) {
        Page<ProductReviewEntity> reviews = reviewRepository.findByProductId(
                productId, PageRequest.of(Math.max(page, 0), size, Sort.by("createdAt").descending()));

        return PageResponse.<ProductReviewResponse>builder()
                .data(reviews.getContent().stream().map(this::toResponse).toList())
                .pageNo(page)
                .pageSize(size)
                .totalItems(reviews.getTotalElements())
                .totalPages(reviews.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public ProductReviewResponse updateReview(Long reviewId, ProductReviewRequest request) {
        Users user = authenticationUtils.getCurrentUser();
        if (user == null)
            throw new ForBiddenException("User not authenticated");

        ProductReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForBiddenException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Users user = authenticationUtils.getCurrentUser();
        if (user == null)
            throw new ForBiddenException("User not authenticated");

        ProductReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForBiddenException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    private ProductReviewResponse toResponse(ProductReviewEntity review) {
        return ProductReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
