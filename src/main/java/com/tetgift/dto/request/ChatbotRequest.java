package com.tetgift.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for chatbot conversation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequest {

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    /**
     * Session ID for continuing conversation (optional)
     */
    private String sessionId;

    /**
     * User ID for authenticated users (optional, set automatically if authenticated)
     */
    private Long userId;
}

