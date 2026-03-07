package com.tetgift.repository.jpa;

import com.tetgift.model.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findByIsActiveTrue(Pageable pageable);

    Optional<ProductEntity> findByIdAndIsActiveTrue(Long id);

    Page<ProductEntity> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);
}
