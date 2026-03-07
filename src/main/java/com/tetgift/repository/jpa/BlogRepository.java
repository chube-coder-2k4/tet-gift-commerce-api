package com.tetgift.repository.jpa;

import com.tetgift.model.entity.BlogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<BlogEntity, Long> {
    Page<BlogEntity> findByTopicId(Long topicId, Pageable pageable);
}
