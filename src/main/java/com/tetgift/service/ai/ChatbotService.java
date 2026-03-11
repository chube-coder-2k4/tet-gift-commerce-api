package com.tetgift.service.ai;

import com.tetgift.dto.request.ChatbotRequest;
import com.tetgift.dto.response.ChatbotResponse;
import com.tetgift.dto.response.ChatbotResponse.ProductSuggestion;
import com.tetgift.dto.response.IntentResult;
import com.tetgift.dto.response.IntentResult.IntentType;
import com.tetgift.model.Users;
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
import java.util.List;
import java.util.UUID;

/**
 * Core Chatbot Service - RAG Pipeline Orchestrator
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "true", matchIfMissing = true)
public class ChatbotService {

    private final ChatClient.Builder chatClientBuilder;
    private final IntentClassifierService intentClassifierService;
    private final EmbeddingService embeddingService;
    private final ProductRepository productRepository;
    private final BundleRepository bundleRepository;
    private final DiscountRepository discountRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AuthenticationUtils authenticationUtils;

    @Value("${chatbot.fallback-message:Xin lỗi, hệ thống AI đang bận. Vui lòng thử lại sau.}")
    private String fallbackMessage;

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

            // 3. Classify intent
            IntentResult intent = intentClassifierService.classifyIntent(request.getMessage());
            log.info("Detected intent: {}", intent.getIntent());

            // 4. Retrieve relevant data based on intent
            String context = retrieveContext(intent);
            List<ProductSuggestion> suggestions = retrieveSuggestions(intent);

            // 5. Generate response using LLM
            String response = generateResponse(request.getMessage(), context, intent);

            // 6. Save assistant message
            saveMessage(session, MessageRole.ASSISTANT, response, intent.getIntent().name());

            return ChatbotResponse.success(session.getSessionToken(), response, intent.getIntent().name(), suggestions);

        } catch (Exception e) {
            log.error("Error processing chatbot request", e);
            return ChatbotResponse.fallback(fallbackMessage);
        }
    }

    private ChatSessionEntity getOrCreateSession(ChatbotRequest request) {
        // Try to find existing session by token
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

    private String retrieveContext(IntentResult intent) {
        StringBuilder context = new StringBuilder();

        switch (intent.getIntent()) {
            case PRODUCT_SEARCH -> {
                List<ProductEntity> products = retrieveProducts(intent);
                List<Document> semanticResults = embeddingService.searchSimilar(intent.getRawQuery(), 5);

                context.append("=== SẢN PHẨM TÌM THẤY ===\n");

                if (!products.isEmpty()) {
                    context.append("\nKết quả từ database:\n");
                    for (ProductEntity p : products) {
                        context.append(formatProduct(p)).append("\n");
                    }
                }

                if (!semanticResults.isEmpty()) {
                    context.append("\nKết quả tìm kiếm liên quan:\n");
                    for (Document doc : semanticResults) {
                        context.append(doc.getText()).append("\n---\n");
                    }
                }

                if (products.isEmpty() && semanticResults.isEmpty()) {
                    context.append("Không tìm thấy sản phẩm phù hợp.\n");
                }
            }

            case BUNDLE_SEARCH -> {
                context.append("=== COMBO/GIỎ QUÀ TẾT ===\n");
                List<BundleEntity> bundles = bundleRepository.findByIsActiveTrue(PageRequest.of(0, 10)).getContent();
                for (BundleEntity b : bundles) {
                    context.append(formatBundle(b)).append("\n");
                }
            }

            case CATEGORY_BROWSE -> {
                context.append("=== SẢN PHẨM THEO DANH MỤC ===\n");
                if (intent.getCategory() != null) {
                    List<ProductEntity> products = productRepository.findByCategoryNameContaining(intent.getCategory());
                    for (ProductEntity p : products) {
                        context.append(formatProduct(p)).append("\n");
                    }
                }
            }

            case STOCK_CHECK -> {
                List<ProductEntity> products = retrieveProducts(intent);
                context.append("=== THÔNG TIN TỒN KHO ===\n");
                for (ProductEntity p : products) {
                    context.append(String.format("- %s: Còn %d sản phẩm (Giá: %s VNĐ)\n",
                        p.getName(), p.getStock(), p.getPrice()));
                }
            }

            case DISCOUNT_POLICY -> {
                context.append("=== CHÍNH SÁCH GIẢM GIÁ ===\n");
                context.append("1. Giảm giá theo số lượng:\n");
                context.append("   - 10-49 sản phẩm: Giảm 5%\n");
                context.append("   - 50-99 sản phẩm: Giảm 10%\n");
                context.append("   - 100+ sản phẩm: Giảm 15% + Free ship\n\n");

                // Add active discounts
                var activeDiscounts = discountRepository.findAll().stream()
                    .filter(DiscountEntity::isActive)
                    .limit(5)
                    .toList();
                if (!activeDiscounts.isEmpty()) {
                    context.append("2. Mã giảm giá đang áp dụng:\n");
                    for (var d : activeDiscounts) {
                        context.append(String.format("   - Mã %s: Giảm %s VNĐ\n", d.getCode(), d.getDiscountValue()));
                    }
                }
            }

            case SHOP_INFO -> {
                context.append("=== THÔNG TIN CỬA HÀNG ===\n");
                context.append("Tên: Shop Quà Tết\n");
                context.append("Hotline: 1900-1234\n");
                context.append("Website: shophuypro.store\n");
                context.append("Email: contact@shophuypro.store\n");
                context.append("Giao hàng: Toàn quốc, miễn phí ship cho đơn từ 100 sản phẩm\n");
            }

            case GENERAL_CHAT -> {
                // For general chat, use semantic search to find relevant context
                List<Document> results = embeddingService.searchSimilar(intent.getRawQuery(), 3);
                if (!results.isEmpty()) {
                    context.append("=== THÔNG TIN LIÊN QUAN ===\n");
                    for (Document doc : results) {
                        context.append(doc.getText()).append("\n---\n");
                    }
                }
            }
        }

        return context.toString();
    }

    private List<ProductEntity> retrieveProducts(IntentResult intent) {
        if (intent.getMaxPrice() != null && intent.getMinPrice() != null) {
            return productRepository.findByPriceBetween(intent.getMinPrice(), intent.getMaxPrice());
        }

        if (intent.getMaxPrice() != null && intent.getQuantity() != null) {
            return productRepository.findByPriceAndAvailability(intent.getMaxPrice(), intent.getQuantity());
        }

        if (intent.getMaxPrice() != null) {
            return productRepository.findByPriceLessThanEqual(intent.getMaxPrice());
        }

        if (intent.getCategory() != null) {
            return productRepository.findByCategoryNameContaining(intent.getCategory());
        }

        if (intent.getKeyword() != null) {
            return productRepository.searchByKeyword(intent.getKeyword());
        }

        // Default: return top 10 products
        return productRepository.findTopActiveProducts(PageRequest.of(0, 10));
    }

    private List<ProductSuggestion> retrieveSuggestions(IntentResult intent) {
        List<ProductSuggestion> suggestions = new ArrayList<>();

        if (intent.getIntent() == IntentType.PRODUCT_SEARCH ||
            intent.getIntent() == IntentType.CATEGORY_BROWSE ||
            intent.getIntent() == IntentType.STOCK_CHECK) {

            List<ProductEntity> products = retrieveProducts(intent).stream().limit(5).toList();
            for (ProductEntity p : products) {
                String imageUrl = null;
                if (p.getProductImages() != null && !p.getProductImages().isEmpty()) {
                    imageUrl = p.getProductImages().stream()
                        .filter(ProductImageEntity::isPrimary)
                        .findFirst()
                        .map(ProductImageEntity::getImageUrl)
                        .orElse(p.getProductImages().get(0).getImageUrl());
                }

                suggestions.add(ProductSuggestion.builder()
                    .id(p.getId())
                    .type("PRODUCT")
                    .name(p.getName())
                    .price(p.getPrice().toString())
                    .stock(p.getStock())
                    .imageUrl(imageUrl)
                    .build());
            }
        }

        if (intent.getIntent() == IntentType.BUNDLE_SEARCH) {
            List<BundleEntity> bundles = bundleRepository.findByIsActiveTrue(PageRequest.of(0, 5)).getContent();
            for (BundleEntity b : bundles) {
                suggestions.add(ProductSuggestion.builder()
                    .id(b.getId())
                    .type("BUNDLE")
                    .name(b.getName())
                    .price(b.getPrice().toString())
                    .build());
            }
        }

        return suggestions;
    }

    private String formatProduct(ProductEntity p) {
        return String.format("• %s - Giá: %s VNĐ - Danh mục: %s - Còn: %d sản phẩm",
            p.getName(),
            p.getPrice(),
            p.getCategory() != null ? p.getCategory().getName() : "Chưa phân loại",
            p.getStock());
    }

    private String formatBundle(BundleEntity b) {
        return String.format("• %s - Giá: %s VNĐ - Loại: %s",
            b.getName(),
            b.getPrice(),
            b.isCustom() ? "Combo tùy chỉnh" : "Combo có sẵn");
    }

    private String generateResponse(String userMessage, String context, IntentResult intent) {
        ChatClient chatClient = chatClientBuilder.build();

        String augmentedPrompt = String.format("""
            %s
            
            === CONTEXT ===
            %s
            
            === CÂU HỎI KHÁCH HÀNG ===
            %s
            
            Hãy trả lời câu hỏi dựa trên context ở trên. Nếu không có thông tin trong context, hãy nói rõ và gợi ý liên hệ hotline 1900-1234.
            """, SYSTEM_PROMPT, context, userMessage);

        return chatClient.prompt()
            .user(augmentedPrompt)
            .call()
            .content();
    }

    /**
     * Get conversation history for a session
     */
    public List<ChatMessageEntity> getHistory(String sessionToken) {
        return chatSessionRepository.findBySessionToken(sessionToken)
            .map(session -> chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()))
            .orElse(List.of());
    }
}


