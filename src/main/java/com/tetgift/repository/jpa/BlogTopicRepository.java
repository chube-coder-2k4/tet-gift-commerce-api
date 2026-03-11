package com.tetgift.repository.jpa;

import com.tetgift.model.entity.BlogTopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogTopicRepository extends JpaRepository<BlogTopicEntity, Long> {
}
