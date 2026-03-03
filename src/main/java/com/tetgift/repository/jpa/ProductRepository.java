package com.tetgift.repository.jpa;

import com.tetgift.model.entity.ProductEntity;
import com.tetgift.model.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findByStatus(ProductStatus status, Pageable pageable);
    Optional<ProductEntity> findByStatusAndId(ProductStatus status, Long id);
}

