package com.tetgift.repository.jpa;

import com.tetgift.model.entity.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<DiscountEntity, Long> {
    Optional<DiscountEntity> findByCodeAndIsActiveTrue(String code);

    List<DiscountEntity> findByIsActiveTrue();
}
