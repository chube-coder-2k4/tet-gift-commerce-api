package com.tetgift.repository.jpa;

import com.tetgift.model.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<ChatMessageEntity> findTop10BySessionIdOrderByCreatedAtDesc(Long sessionId);
}

