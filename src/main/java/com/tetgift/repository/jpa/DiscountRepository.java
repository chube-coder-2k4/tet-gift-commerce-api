package com.tetgift.repository.jpa;

import com.tetgift.model.entity.DiscountEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<DiscountEntity, Long> {
    Optional<DiscountEntity> findByCodeAndIsActiveTrue(String code);

    @Query("SELECT d FROM DiscountEntity d WHERE d.isActive = true AND d.endDate > CURRENT_TIMESTAMP ORDER BY d.discountValue DESC")
    List<DiscountEntity> findActiveAndNotExpired(Pageable pageable);
}
