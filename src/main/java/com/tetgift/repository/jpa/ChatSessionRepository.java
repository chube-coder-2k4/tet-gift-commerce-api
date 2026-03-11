package com.tetgift.repository.jpa;

import com.tetgift.model.entity.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, Long> {

    Optional<ChatSessionEntity> findBySessionToken(String sessionToken);

    Optional<ChatSessionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}

