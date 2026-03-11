package com.tetgift.repository.jpa;

import com.tetgift.model.entity.ProductReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReviewEntity, Long> {
    Page<ProductReviewEntity> findByProductId(Long productId, Pageable pageable);

    Optional<ProductReviewEntity> findByProductIdAndUserId(Long productId, Long userId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);
}
