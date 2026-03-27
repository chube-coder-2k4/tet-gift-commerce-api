package com.tetgift.controller;

import com.tetgift.dto.request.ChatbotRequest;
import com.tetgift.dto.response.ChatbotResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.model.entity.ChatMessageEntity;
import com.tetgift.service.ai.ChatbotService;
import com.tetgift.service.ai.EmbeddingService;
import com.tetgift.util.AuthenticationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for AI Chatbot
 */
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chatbot", description = "APIs for AI-powered product consultation chatbot")
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "true", matchIfMissing = true)
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final EmbeddingService embeddingService;
    private final AuthenticationUtils authenticationUtils;

    /**
     * Send a message and get AI response
     */
    @PostMapping("/chat")
    @Operation(summary = "Chat with AI", description = "Send a message and receive AI-powered product consultation")
    public ResponseEntity<ResponseData<ChatbotResponse>> chat(@Valid @RequestBody ChatbotRequest request) {
        log.info("Received chat request: {}", request.getMessage());

        // Set userId from authenticated user if available
        Long currentUserId = authenticationUtils.getCurrentUserId();
        if (currentUserId != null) {
            request.setUserId(currentUserId);
        }

        ChatbotResponse response = chatbotService.chat(request);

        return ResponseEntity.ok(new ResponseData<>(
            HttpStatus.OK.value(),
            "Chat processed successfully",
            response
        ));
    }

    /**
     * Stream chat response via Server-Sent Events (SSE).
     * Response is streamed token-by-token for real-time typing effect.
     * 
     * SSE Events:
     * - data: [SESSION]{sessionId}        → session identifier
     * - data: {token}                     → each generated token
     * - data: [SUGGESTIONS]{json}         → product suggestion cards (JSON)
     * - data: [DONE]                      → stream complete
     * - data: [ERROR]{message}            → error occurred
     */
    @PostMapping(value = "/chat/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Chat with AI (Streaming)", description = "Stream AI response token-by-token via SSE for real-time typing effect")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chatStream(@Valid @RequestBody ChatbotRequest request) {
        log.info("Received streaming chat request: {}", request.getMessage());

        // Set userId from authenticated user if available
        Long currentUserId = authenticationUtils.getCurrentUserId();
        if (currentUserId != null) {
            request.setUserId(currentUserId);
        }

        return chatbotService.chatStream(request);
    }

    /**
     * Get conversation history
     */
    @GetMapping("/history/{sessionId}")
    @Operation(summary = "Get chat history", description = "Retrieve conversation history for a session")
    public ResponseEntity<ResponseData<List<ChatMessageEntity>>> getHistory(@PathVariable String sessionId) {
        List<ChatMessageEntity> history = chatbotService.getHistory(sessionId);

        return ResponseEntity.ok(new ResponseData<>(
            HttpStatus.OK.value(),
            "History fetched successfully",
            history
        ));
    }

    /**
     * Sync product embeddings (Admin only)
     */
    @PostMapping("/embeddings/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Sync embeddings (ADMIN)", description = "Re-generate embeddings for all products and bundles")
    public ResponseEntity<ResponseData<Map<String, Integer>>> syncEmbeddings() {
        log.info("Starting embeddings sync...");

        embeddingService.clearAllEmbeddings();
        int productCount = embeddingService.embedAllProducts();
        int bundleCount = embeddingService.embedAllBundles();

        Map<String, Integer> result = Map.of(
            "productsEmbedded", productCount,
            "bundlesEmbedded", bundleCount
        );

        return ResponseEntity.ok(new ResponseData<>(
            HttpStatus.OK.value(),
            "Embeddings synced successfully",
            result
        ));
    }
}
