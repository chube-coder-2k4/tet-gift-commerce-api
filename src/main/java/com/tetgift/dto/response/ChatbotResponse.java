package com.tetgift.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for chatbot conversation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {

    private String sessionId;
    private String message;
    private LocalDateTime timestamp;
    private String detectedIntent;
    private boolean success;
    private String errorMessage;
    private List<ProductSuggestion> suggestions;

    public static ChatbotResponse success(String sessionId, String message, String intent) {
        return ChatbotResponse.builder()
                .sessionId(sessionId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .detectedIntent(intent)
                .success(true)
                .build();
    }

    public static ChatbotResponse success(String sessionId, String message, String intent, List<ProductSuggestion> suggestions) {
        return ChatbotResponse.builder()
                .sessionId(sessionId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .detectedIntent(intent)
                .success(true)
                .suggestions(suggestions)
                .build();
    }

    public static ChatbotResponse error(String errorMessage) {
        return ChatbotResponse.builder()
                .message("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.")
                .timestamp(LocalDateTime.now())
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    public static ChatbotResponse fallback(String fallbackMessage) {
        return ChatbotResponse.builder()
                .message(fallbackMessage)
                .timestamp(LocalDateTime.now())
                .success(false)
                .detectedIntent("FALLBACK")
                .build();
    }

    /**
     * Product suggestion included in response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSuggestion {
        private Long id;
        private String type; // "PRODUCT" or "BUNDLE"
        private String name;
        private String price;
        private Integer stock;
        private String imageUrl;
    }
}

