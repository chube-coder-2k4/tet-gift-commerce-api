package com.tetgift.repository.jpa;

import com.tetgift.model.entity.BundleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BundleRepository extends JpaRepository<BundleEntity, Long> {
    Page<BundleEntity> findByIsActiveTrue(Pageable pageable);

    Optional<BundleEntity> findByIdAndIsActiveTrue(Long id);
}
