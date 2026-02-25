package com.tetgift.repository.jpa;

import com.tetgift.model.entity.ProductBadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductBadgeRepository extends JpaRepository<ProductBadgeEntity, Long> {
}
