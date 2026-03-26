package com.tetgift.service.ai;

import com.tetgift.dto.request.ChatbotRequest;
import com.tetgift.dto.response.ChatbotResponse;
import com.tetgift.dto.response.ChatbotResponse.ProductSuggestion;
import com.tetgift.model.Users;
import com.tetgift.model.Category;
import com.tetgift.model.entity.*;
import com.tetgift.model.entity.ChatMessageEntity.MessageRole;
import com.tetgift.repository.jpa.*;
import com.tetgift.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core Chatbot Service - RAG Pipeline Orchestrator
 * 
 * Improvements over v1:
 * - Single LLM call instead of 2 (no separate intent classification)
 * - Conversation history context (last 8 messages)
 * - Category fallback when no category is detected
 * - Bundle suggestions include image
 * - Optimized discount query
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "true", matchIfMissing = true)
public class ChatbotService {

    private final ChatClient.Builder chatClientBuilder;
    private final EmbeddingService embeddingService;
    private final ProductRepository productRepository;
    private final BundleRepository bundleRepository;
    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AuthenticationUtils authenticationUtils;

    @Value("${chatbot.fallback-message:Xin lỗi, hệ thống AI đang bận. Vui lòng thử lại sau.}")
    private String fallbackMessage;

    private static final int MAX_HISTORY_MESSAGES = 8;

    private static final String SYSTEM_PROMPT = """
        Bạn là nhân viên tư vấn bán hàng của Shop Quà Tết - chuyên bán giỏ quà Tết, bánh kẹo, rượu vang, mứt Tết.
        
        NHIỆM VỤ:
        1. Tư vấn và gợi ý sản phẩm quà Tết phù hợp với nhu cầu khách hàng
        2. Trả lời câu hỏi về sản phẩm, giá cả, tồn kho
        3. Hỗ trợ thông tin về chính sách giảm giá, đặt hàng
        
        QUY TẮC QUAN TRỌNG:
        - KHÔNG được bịa thông tin về sản phẩm, giá, tồn kho
        - CHỈ sử dụng thông tin từ context được cung cấp
        - Nếu không có thông tin, hãy nói rõ và đề xuất liên hệ hotline
        - Trả lời lịch sự, thân thiện, bằng tiếng Việt
        - Khi gợi ý sản phẩm, đưa ra tối đa 5 sản phẩm phù hợp nhất
        - Khi nói về giá, format đẹp: 1.500.000 VNĐ
        
        THÔNG TIN CỬA HÀNG:
        - Tên: Shop Quà Tết
        - Hotline: 1900-1234
        - Website: shophuypro.store
        - Giao hàng: Toàn quốc
        
        CHÍNH SÁCH GIẢM GIÁ:
        - Mua từ 10 sản phẩm: Giảm 5%%
        - Mua từ 50 sản phẩm: Giảm 10%%
        - Mua từ 100 sản phẩm: Giảm 15%% + Free ship
        """;

    @Transactional
    public ChatbotResponse chat(ChatbotRequest request) {
        log.info("Processing chatbot request: {}", request.getMessage());

        try {
            // 1. Get or create session
            ChatSessionEntity session = getOrCreateSession(request);

            // 2. Save user message
            saveMessage(session, MessageRole.USER, request.getMessage(), null);

            // 3. Build context from ALL available data sources (replaces separate intent classification)
            String productContext = buildSmartContext(request.getMessage());

            // 4. Load conversation history for continuity
            String conversationHistory = buildConversationHistory(session.getId());

            // 5. Generate response using SINGLE LLM call (intent + response combined)
            String response = generateSmartResponse(request.getMessage(), productContext, conversationHistory);

            // 6. Build product suggestions based on semantic search
            List<ProductSuggestion> suggestions = buildSuggestions(request.getMessage());

            // 7. Save assistant message
            saveMessage(session, MessageRole.ASSISTANT, response, null);

            return ChatbotResponse.success(session.getSessionToken(), response, "AUTO", suggestions);

        } catch (Exception e) {
            log.error("Error processing chatbot request", e);
            return ChatbotResponse.fallback(fallbackMessage);
        }
    }

    // ==================== SESSION MANAGEMENT ====================

    private ChatSessionEntity getOrCreateSession(ChatbotRequest request) {
        if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
            return chatSessionRepository.findBySessionToken(request.getSessionId())
                .orElseGet(() -> createNewSession(request.getUserId()));
        }
        return createNewSession(request.getUserId());
    }

    private ChatSessionEntity createNewSession(Long userId) {
        Users user = null;
        if (userId != null) {
            user = authenticationUtils.getCurrentUser();
        }

        ChatSessionEntity session = ChatSessionEntity.builder()
            .sessionToken(UUID.randomUUID().toString())
            .user(user)
            .build();
        return chatSessionRepository.save(session);
    }

    private void saveMessage(ChatSessionEntity session, MessageRole role, String content, String intent) {
        ChatMessageEntity message = ChatMessageEntity.builder()
            .session(session)
            .role(role)
            .content(content)
            .intent(intent)
            .build();
        chatMessageRepository.save(message);
    }

    // ==================== CONVERSATION HISTORY (FIX #2) ====================

    /**
     * Build conversation history string from last N messages.
     * This enables the chatbot to maintain context across turns.
     */
    private String buildConversationHistory(Long sessionId) {
        List<ChatMessageEntity> recentMessages = chatMessageRepository
            .findTop10BySessionIdOrderByCreatedAtDesc(sessionId);

        if (recentMessages.isEmpty()) {
            return "";
        }

        // Reverse to chronological order, take last MAX_HISTORY_MESSAGES (excluding the current user message we just saved)
        List<ChatMessageEntity> chronological = new ArrayList<>(recentMessages);
        Collections.reverse(chronological);

        // Skip the very last message (the current user message we just saved in step 2)
        if (!chronological.isEmpty()) {
            chronological = chronological.subList(0, Math.max(0, chronological.size() - 1));
        }

        // Take only last MAX_HISTORY_MESSAGES
        if (chronological.size() > MAX_HISTORY_MESSAGES) {
            chronological = chronological.subList(chronological.size() - MAX_HISTORY_MESSAGES, chronological.size());
        }

        if (chronological.isEmpty()) {
            return "";
        }

        StringBuilder history = new StringBuilder("=== LỊCH SỬ HỘI THOẠI GẦN ĐÂY ===\n");
        for (ChatMessageEntity msg : chronological) {
            String role = msg.getRole() == MessageRole.USER ? "Khách" : "Tư vấn viên";
            history.append(role).append(": ").append(msg.getContent()).append("\n");
        }
        history.append("=== HẾT LỊCH SỬ ===\n");

        return history.toString();
    }

    // ==================== SMART CONTEXT (FIX #1 - eliminates separate intent LLM call) ====================

    /**
     * Build context by searching ALL relevant data sources at once.
     * This replaces the separate IntentClassifierService LLM call.
     * Instead of: LLM classify → specific query, we do: broad query → let LLM figure it out.
     */
    private String buildSmartContext(String userMessage) {
        StringBuilder context = new StringBuilder();

        // 1. Semantic search (always - this is the core RAG benefit)
        List<Document> semanticResults = embeddingService.searchSimilar(userMessage, 5);
        if (!semanticResults.isEmpty()) {
            context.append("=== SẢN PHẨM & COMBO LIÊN QUAN ===\n");
            for (Document doc : semanticResults) {
                context.append(doc.getText()).append("\n---\n");
            }
        }

        // 2. Keyword-based product search (supplement semantic search)
        List<ProductEntity> keywordProducts = productRepository.searchByKeyword(userMessage);
        if (!keywordProducts.isEmpty()) {
            context.append("\n=== KẾT QUẢ TÌM KIẾM ===\n");
            for (ProductEntity p : keywordProducts.stream().limit(5).toList()) {
                context.append(formatProduct(p)).append("\n");
            }
        }

        // 3. Active bundles (for combo/gift-related queries)
        if (containsAny(userMessage, "combo", "giỏ", "set", "bộ", "quà", "tặng", "gift")) {
            List<BundleEntity> bundles = bundleRepository.findByIsActiveTrue(PageRequest.of(0, 5)).getContent();
            if (!bundles.isEmpty()) {
                context.append("\n=== COMBO/GIỎ QUÀ TẾT ===\n");
                for (BundleEntity b : bundles) {
                    context.append(formatBundle(b)).append("\n");
                }
            }
        }

        // 4. Category listing (FIX #4 - always provide available categories for context)
        List<Category> activeCategories = categoryRepository.findByIsActiveTrue();
        if (!activeCategories.isEmpty()) {
            context.append("\n=== DANH MỤC SẢN PHẨM ===\n");
            for (Category c : activeCategories) {
                context.append("• ").append(c.getName());
                if (c.getDescription() != null) {
                    context.append(" - ").append(c.getDescription());
                }
                context.append("\n");
            }
        }

        // 5. Active discounts (FIX #5 - use proper query instead of findAll)
        if (containsAny(userMessage, "giảm", "khuyến", "mã", "sale", "discount", "voucher", "ưu đãi")) {
            List<DiscountEntity> activeDiscounts = discountRepository.findByIsActiveTrue();
            if (!activeDiscounts.isEmpty()) {
                context.append("\n=== MÃ GIẢM GIÁ ĐANG ÁP DỤNG ===\n");
                for (DiscountEntity d : activeDiscounts.stream().limit(5).toList()) {
                    context.append(String.format("• Mã %s: Giảm %s VNĐ (Đơn tối thiểu: %s VNĐ)\n",
                        d.getCode(), d.getDiscountValue(),
                        d.getMinOrderAmount() != null ? d.getMinOrderAmount() : "Không yêu cầu"));
                }
            }
        }

        // 6. If context is still empty, provide top products as fallback
        if (context.isEmpty()) {
            List<ProductEntity> topProducts = productRepository.findTopActiveProducts(PageRequest.of(0, 10));
            context.append("=== SẢN PHẨM NỔI BẬT ===\n");
            for (ProductEntity p : topProducts) {
                context.append(formatProduct(p)).append("\n");
            }
        }

        return context.toString();
    }

    // ==================== SMART RESPONSE (FIX #1 - single LLM call) ====================

    /**
     * Generate response using a SINGLE LLM call.
     * The LLM handles intent understanding + response generation in one go.
     * This halves the latency compared to the 2-call approach.
     */
    private String generateSmartResponse(String userMessage, String productContext, String conversationHistory) {
        ChatClient chatClient = chatClientBuilder.build();
        String augmentedPrompt = buildAugmentedPrompt(userMessage, productContext, conversationHistory);

        return chatClient.prompt()
            .user(augmentedPrompt)
            .call()
            .content();
    }

    // ==================== PRODUCT SUGGESTIONS (FIX #6 - bundle image) ====================

    /**
     * Build product/bundle suggestions based on semantic similarity.
     * These are structured data that FE can render as clickable cards.
     */
    private List<ProductSuggestion> buildSuggestions(String userMessage) {
        List<ProductSuggestion> suggestions = new ArrayList<>();

        // Semantic search for relevant products
        List<Document> results = embeddingService.searchSimilar(userMessage, 5);
        for (Document doc : results) {
            String type = doc.getMetadata().getOrDefault("type", "PRODUCT").toString();
            String idStr = doc.getMetadata().getOrDefault("id", "0").toString();

            try {
                Long id = Long.parseLong(idStr);

                if ("PRODUCT".equals(type)) {
                    productRepository.findByIdAndIsActiveTrue(id).ifPresent(p -> {
                        String imageUrl = extractPrimaryImage(p);
                        suggestions.add(ProductSuggestion.builder()
                            .id(p.getId())
                            .type("PRODUCT")
                            .name(p.getName())
                            .price(p.getPrice().toString())
                            .stock(p.getStock())
                            .imageUrl(imageUrl)
                            .build());
                    });
                } else if ("BUNDLE".equals(type)) {
                    bundleRepository.findById(id)
                        .filter(BundleEntity::isActive)
                        .ifPresent(b -> {
                            suggestions.add(ProductSuggestion.builder()
                                .id(b.getId())
                                .type("BUNDLE")
                                .name(b.getName())
                                .price(b.getPrice().toString())
                                .imageUrl(b.getImage())  // FIX #6: include bundle image
                                .build());
                        });
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid id in document metadata: {}", idStr);
            }
        }

        return suggestions.stream().limit(5).collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private String extractPrimaryImage(ProductEntity product) {
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            return product.getProductImages().stream()
                .filter(ProductImageEntity::isPrimary)
                .findFirst()
                .map(ProductImageEntity::getImageUrl)
                .orElse(product.getProductImages().get(0).getImageUrl());
        }
        return product.getImage(); // fallback to legacy image field
    }

    private String formatProduct(ProductEntity p) {
        return String.format("• %s - Giá: %s VNĐ - Danh mục: %s - Còn: %d sản phẩm",
            p.getName(),
            p.getPrice(),
            p.getCategory() != null ? p.getCategory().getName() : "Chưa phân loại",
            p.getStock());
    }

    private String formatBundle(BundleEntity b) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("• %s - Giá: %s VNĐ - Loại: %s",
            b.getName(),
            b.getPrice(),
            b.isCustom() ? "Combo tùy chỉnh" : "Combo có sẵn"));

        // Include bundle products for better LLM context
        if (b.getBundleProducts() != null && !b.getBundleProducts().isEmpty()) {
            sb.append(" | Bao gồm: ");
            b.getBundleProducts().forEach(bp -> {
                if (bp.getProduct() != null) {
                    sb.append(bp.getProduct().getName()).append(" (x").append(bp.getQuantity()).append("), ");
                }
            });
        }
        return sb.toString();
    }

    /**
     * Simple keyword matching helper — avoids needing a separate LLM call for intent classification.
     */
    private boolean containsAny(String text, String... keywords) {
        String lower = text.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get conversation history for a session
     */
    public List<ChatMessageEntity> getHistory(String sessionToken) {
        return chatSessionRepository.findBySessionToken(sessionToken)
            .map(session -> chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()))
            .orElse(List.of());
    }

    // ==================== STREAMING (FIX #3) ====================

    /**
     * Stream chatbot response via SseEmitter.
     * 
     * SSE Event format:
     * - data: {token}           → each token as it's generated
     * - data: [SESSION]{id}     → session ID (first event)
     * - data: [SUGGESTIONS]{json} → product suggestions (after response complete)
     * - data: [DONE]            → stream complete signal
     * - data: [ERROR]{message}  → error occurred
     */
    @Transactional
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chatStream(ChatbotRequest request) {
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = 
            new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(120_000L); // 2 min timeout

        // Run async to not block the request thread
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // 1. Get or create session
                ChatSessionEntity session = getOrCreateSession(request);
                
                // Send session ID as first event
                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter
                    .event().data("[SESSION]" + session.getSessionToken()));

                // 2. Save user message
                saveMessage(session, MessageRole.USER, request.getMessage(), null);

                // 3. Build context
                String productContext = buildSmartContext(request.getMessage());
                String conversationHistory = buildConversationHistory(session.getId());

                // 4. Build the augmented prompt (same as non-streaming)
                String augmentedPrompt = buildAugmentedPrompt(request.getMessage(), productContext, conversationHistory);

                // 5. Stream response tokens
                ChatClient chatClient = chatClientBuilder.build();
                StringBuilder fullResponse = new StringBuilder();

                reactor.core.publisher.Flux<String> tokenStream = chatClient.prompt()
                    .user(augmentedPrompt)
                    .stream()
                    .content();

                tokenStream.doOnNext(token -> {
                    try {
                        fullResponse.append(token);
                        emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter
                            .event().data(token));
                    } catch (Exception e) {
                        log.error("Error sending SSE token", e);
                    }
                }).doOnComplete(() -> {
                    try {
                        // Save full response to DB
                        saveMessage(session, MessageRole.ASSISTANT, fullResponse.toString(), null);

                        // Send product suggestions
                        List<ProductSuggestion> suggestions = buildSuggestions(request.getMessage());
                        if (!suggestions.isEmpty()) {
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            String suggestionsJson = mapper.writeValueAsString(suggestions);
                            emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter
                                .event().data("[SUGGESTIONS]" + suggestionsJson));
                        }

                        // Send done signal
                        emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter
                            .event().data("[DONE]"));
                        emitter.complete();

                        log.info("Streaming response completed for session: {}", session.getSessionToken());
                    } catch (Exception e) {
                        log.error("Error completing SSE stream", e);
                        emitter.completeWithError(e);
                    }
                }).doOnError(error -> {
                    try {
                        log.error("Error in LLM stream", error);
                        emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter
                            .event().data("[ERROR]" + fallbackMessage));
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }).subscribe();

            } catch (Exception e) {
                try {
                    log.error("Error in chatStream setup", e);
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter
                        .event().data("[ERROR]" + fallbackMessage));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE stream timed out");
            emitter.complete();
        });

        return emitter;
    }

    /**
     * Build the augmented prompt for LLM (shared between streaming and non-streaming).
     */
    private String buildAugmentedPrompt(String userMessage, String productContext, String conversationHistory) {
        return String.format("""
            %s
            
            %s
            
            === THÔNG TIN SẢN PHẨM & CỬA HÀNG ===
            %s
            
            === CÂU HỎI HIỆN TẠI CỦA KHÁCH HÀNG ===
            %s
            
            Hãy dựa vào lịch sử hội thoại (nếu có) và thông tin sản phẩm ở trên để trả lời câu hỏi.
            Nếu khách hỏi tham chiếu đến sản phẩm trước đó (ví dụ: "cái đó", "cái rẻ hơn"), hãy xem lại lịch sử hội thoại.
            Nếu không có thông tin, hãy nói rõ và gợi ý liên hệ hotline 1900-1234.
            """, SYSTEM_PROMPT, conversationHistory, productContext, userMessage);
    }
}
