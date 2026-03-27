package com.tetgift.repository.jpa;

import com.tetgift.model.entity.HomeSlideEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeSlideRepository extends JpaRepository<HomeSlideEntity, Long> {
    List<HomeSlideEntity> findAllByIsActiveTrueOrderBySlideOrderAsc();
    List<HomeSlideEntity> findAllByOrderBySlideOrderAsc();
}
