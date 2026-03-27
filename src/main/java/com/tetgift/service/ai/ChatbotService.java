package com.tetgift.service.ai;

import com.tetgift.dto.request.ChatbotRequest;
import com.tetgift.dto.response.ChatbotResponse;
import com.tetgift.dto.response.ChatbotResponse.ProductSuggestion;
import com.tetgift.dto.response.ConversationState;
import com.tetgift.model.Users;
import com.tetgift.model.Category;
import com.tetgift.model.entity.*;
import com.tetgift.model.entity.ChatMessageEntity.MessageRole;
import com.tetgift.repository.jpa.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise Orchestrator Service (Slot Filling + Recommendations)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "true", matchIfMissing = true)
public class ChatbotService {

    private final ChatClient.Builder chatClientBuilder;
    private final IntentParserService intentParserService;
    private final RecommendationService recommendationService;
    private final ProductRepository productRepository;
    private final BundleRepository bundleRepository;
    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @Value("${chatbot.fallback-message:Xin lỗi, hệ thống AI đang bận. Vui lòng thử lại sau.}")
    private String fallbackMessage;

    private static final int MAX_HISTORY_MESSAGES = 8;

    private static final String SYSTEM_PROMPT = """
        Bạn là nhân viên tư vấn bán hàng của Shop Quà Tết - chuyên bán giỏ quà Tết, bánh kẹo, rượu vang, mứt Tết.
        
        NHIỆM VỤ:
        1. Tư vấn và gợi ý sản phẩm quà Tết phù hợp với nhu cầu khách hàng dựa trên SẢN PHẨM TỪ HỆ THỐNG GỢI Ý.
        2. Trả lời câu hỏi về sản phẩm, giá cả, tồn kho.
        3. Hỗ trợ thông tin về chính sách giảm giá, đặt hàng.
        
        QUY TẮC QUAN TRỌNG:
        - KHÔNG được bịa thông tin về sản phẩm, giá, tồn kho.
        - CHỈ sử dụng thông tin từ context được cung cấp.
        - Trả lời lịch sự, thân thiện, mang không khí Tết và CÓ KÈM emoji sinh động.
        - TỐI ƯU UX: KHÔNG liệt kê dài dòng thông số hay giá cả của sản phẩm, CHỈ cần tóm tắt tại sao sản phẩm đó phù hợp hoặc giới thiệu ngắn gọn. (Hệ thống đã tự động hiển thị Card sản phẩm kèm ảnh/giá ngay bên dưới câu trả lời của bạn rồi).
        - Khi nói về giá, format đẹp: 1.500.000 VNĐ.
        
        THÔNG TIN CỬA HÀNG:
        - Tên: Shop Quà Tết
        - Hotline: 1900-1234
        - Website: shophuypro.store
        - Giao hàng: Toàn quốc
        
        CHÍNH SÁCH GIẢM GIÁ:
        - Mua từ 10 sản phẩm: Giảm 5%
        - Mua từ 50 sản phẩm: Giảm 10%
        - Mua từ 100 sản phẩm: Giảm 15% + Free ship
        """;

    @Transactional
    public ChatbotResponse chat(ChatbotRequest request) {
        log.info("Processing chatbot request: {}", request.getMessage());

        try {
            ChatSessionEntity session = getOrCreateSession(request);
            saveMessage(session, MessageRole.USER, request.getMessage(), null);
            
            String conversationHistory = buildConversationHistory(session.getId());
            
            // 1. Intent & Slot Filling
            ConversationState state = intentParserService.parseIntent(conversationHistory, request.getMessage());
            
            // 2. Slot Filling Bypass (Short-circuit if missing info)
            if ("NEED_INFO".equals(state.getIntent()) && !state.isReadyToRecommend() && state.getMissingSlotPrompt() != null) {
                saveMessage(session, MessageRole.ASSISTANT, state.getMissingSlotPrompt(), state.getIntent());
                return ChatbotResponse.success(session.getSessionToken(), state.getMissingSlotPrompt(), state.getIntent(), new ArrayList<>());
            }
            
            // 3. Recommendation Engine
            List<ProductSuggestion> suggestions = new ArrayList<>();
            if ("RECOMMEND".equals(state.getIntent()) || state.isReadyToRecommend()) {
                suggestions = recommendationService.recommendProducts(request.getMessage(), state);
            }

            // 4. Build Context & LLM Response
            String productContext = buildSmartContext(request.getMessage(), state, suggestions);
            String response = generateSmartResponse(request.getMessage(), productContext, conversationHistory);

            saveMessage(session, MessageRole.ASSISTANT, response, state.getIntent());

            return ChatbotResponse.success(session.getSessionToken(), response, state.getIntent(), suggestions);

        } catch (Exception e) {
            log.error("Error processing chatbot request", e);
            return ChatbotResponse.fallback(fallbackMessage);
        }
    }

    private ChatSessionEntity getOrCreateSession(ChatbotRequest request) {
        if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
            return chatSessionRepository.findBySessionToken(request.getSessionId())
                .orElseGet(() -> createNewSession(request.getUserId()));
        }
        return createNewSession(request.getUserId());
    }

    private ChatSessionEntity createNewSession(Long userId) {
        Users user = userId != null ? userRepository.findById(userId).orElse(null) : null;
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

    private String buildConversationHistory(Long sessionId) {
        List<ChatMessageEntity> recentMessages = chatMessageRepository.findTop10BySessionIdOrderByCreatedAtDesc(sessionId);
        if (recentMessages.isEmpty()) return "";

        List<ChatMessageEntity> chronological = new ArrayList<>(recentMessages);
        Collections.reverse(chronological);

        if (!chronological.isEmpty()) {
            chronological = chronological.subList(0, Math.max(0, chronological.size() - 1));
        }
        if (chronological.size() > MAX_HISTORY_MESSAGES) {
            chronological = chronological.subList(chronological.size() - MAX_HISTORY_MESSAGES, chronological.size());
        }
        if (chronological.isEmpty()) return "";

        StringBuilder history = new StringBuilder("=== LỊCH SỬ GẦN ĐÂY ===\n");
        for (ChatMessageEntity msg : chronological) {
            String role = msg.getRole() == MessageRole.USER ? "Khách" : "Tư vấn viên";
            history.append(role).append(": ").append(msg.getContent()).append("\n");
        }
        return history.toString();
    }

    private String buildSmartContext(String userMessage, ConversationState state, List<ProductSuggestion> explicitRecs) {
        StringBuilder context = new StringBuilder();

        if (explicitRecs != null && !explicitRecs.isEmpty()) {
            context.append("=== SẢN PHẨM KHỚP VỚI KHÁCH HÀNG (HỆ THỐNG GỢI Ý) ===\n");
            for (ProductSuggestion p : explicitRecs) {
                context.append(String.format("• %s - Giá: %s - Trạng thái: %s\n", p.getName(), p.getPrice(), p.getStock() != null ? "Còn " + p.getStock() : "Có sẵn"));
            }
        }

        List<Category> activeCategories = categoryRepository.findByIsActiveTrue();
        if (!activeCategories.isEmpty()) {
            context.append("\n=== DANH MỤC ===\n");
            activeCategories.forEach(c -> context.append("• ").append(c.getName()).append("\n"));
        }

        if (containsAny(userMessage, "giảm", "khuyến", "mã", "sale", "discount", "voucher")) {
            List<DiscountEntity> discounts = discountRepository.findByIsActiveTrue();
            if (!discounts.isEmpty()) {
                context.append("\n=== MÃ GIẢM GIÁ ===\n");
                discounts.stream().limit(5).forEach(d -> 
                    context.append(String.format("• Mã %s: Giảm %s VNĐ\n", d.getCode(), d.getDiscountValue())));
            }
        }

        if (state != null && state.getMaxBudget() != null) {
            context.append("\n=== YÊU CẦU CỦA KHÁCH ===\n");
            context.append("Ngân sách tối đa: ").append(state.getMaxBudget()).append(" VNĐ\n");
            if (state.getRecipient() != null) context.append("Tặng cho: ").append(state.getRecipient()).append("\n");
        }

        return context.toString();
    }

    private String generateSmartResponse(String userMessage, String productContext, String conversationHistory) {
        ChatClient chatClient = chatClientBuilder.build();
        String augmentedPrompt = String.format("""
            %s
            
            %s
            
            === THÔNG TIN SẢN PHẨM & CỬA HÀNG ===
            %s
            
            === CÂU HỎI HIỆN TẠI ===
            %s
            
            Hãy dựa vào lịch sử hội thoại và thông tin sản phẩm trên để trả lời câu hỏi.
            Nếu không có thông tin, hãy nói rõ và gợi ý liên hệ hotline.
            """, SYSTEM_PROMPT, conversationHistory, productContext, userMessage);

        return chatClient.prompt().user(augmentedPrompt).call().content();
    }

    private boolean containsAny(String text, String... keywords) {
        String lower = text.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) return true;
        }
        return false;
    }

    public List<ChatMessageEntity> getHistory(String sessionToken) {
        return chatSessionRepository.findBySessionToken(sessionToken)
            .map(session -> chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()))
            .orElse(List.of());
    }

    @Transactional
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chatStream(ChatbotRequest request) {
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = 
            new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(120_000L);

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                Object[] setupData = transactionTemplate.execute(status -> {
                    ChatSessionEntity s = getOrCreateSession(request);
                    saveMessage(s, MessageRole.USER, request.getMessage(), null);
                    String cHist = buildConversationHistory(s.getId());
                    
                    ConversationState st = intentParserService.parseIntent(cHist, request.getMessage());
                    
                    List<ProductSuggestion> recs = new ArrayList<>();
                    if ("RECOMMEND".equals(st.getIntent()) || st.isReadyToRecommend()) {
                        recs = recommendationService.recommendProducts(request.getMessage(), st);
                    }
                    
                    String pCtx = buildSmartContext(request.getMessage(), st, recs);
                    return new Object[]{s, pCtx, cHist, st, recs};
                });
                
                ChatSessionEntity session = (ChatSessionEntity) setupData[0];
                String productContext = (String) setupData[1];
                String conversationHistory = (String) setupData[2];
                ConversationState state = (ConversationState) setupData[3];
                List<ProductSuggestion> suggestions = (List<ProductSuggestion>) setupData[4];

                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data("[SESSION]" + session.getSessionToken()));

                // Slot Filling Bypass
                if ("NEED_INFO".equals(state.getIntent()) && !state.isReadyToRecommend() && state.getMissingSlotPrompt() != null) {
                    transactionTemplate.execute(status -> {
                        saveMessage(session, MessageRole.ASSISTANT, state.getMissingSlotPrompt(), state.getIntent());
                        return null;
                    });
                    
                    String[] words = state.getMissingSlotPrompt().split(" ");
                    for(String w : words) {
                        emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data(w + " "));
                        Thread.sleep(40);
                    }
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data("[DONE]"));
                    emitter.complete();
                    return;
                }

                String augmentedPrompt = String.format("""
                    %s
                    
                    %s
                    
                    === THÔNG TIN SẢN PHẨM & CỬA HÀNG ===
                    %s
                    
                    === CÂU HỎI HIỆN TẠI ===
                    %s
                    """, SYSTEM_PROMPT, conversationHistory, productContext, request.getMessage());

                ChatClient chatClient = chatClientBuilder.build();
                StringBuilder fullResponse = new StringBuilder();

                chatClient.prompt().user(augmentedPrompt).stream().content().doOnNext(token -> {
                    try {
                        fullResponse.append(token);
                        emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data(token));
                    } catch (Exception ignored) {}
                }).doOnComplete(() -> {
                    try {
                        String suggestionsJson = transactionTemplate.execute(status -> {
                            saveMessage(session, MessageRole.ASSISTANT, fullResponse.toString(), state.getIntent());
                            if (!suggestions.isEmpty()) {
                                try {
                                    return objectMapper.writeValueAsString(suggestions);
                                } catch (Exception ex) { return null; }
                            }
                            return null;
                        });

                        if (suggestionsJson != null) {
                            emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data("[SUGGESTIONS]" + suggestionsJson));
                        }
                        emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }).doOnError(error -> {
                    try {
                        emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data("[ERROR]" + fallbackMessage));
                        emitter.complete();
                    } catch (Exception ignored) {}
                }).subscribe();
            } catch (Exception e) {
                try {
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data("[ERROR]" + fallbackMessage));
                    emitter.complete();
                } catch (Exception ignored) {}
            }
        });

        emitter.onTimeout(emitter::complete);
        return emitter;
    }
}
