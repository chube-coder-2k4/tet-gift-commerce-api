package com.tetgift.service.ai;

import com.tetgift.dto.request.ChatbotRequest;
import com.tetgift.dto.response.ChatbotResponse;
import com.tetgift.dto.response.ChatbotResponse.ProductSuggestion;
import com.tetgift.dto.response.ChatMessageResponse;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            Bạn là nhân viên tư vấn bán hàng thân thiện của **Shop Quà Tết** - chuyên giỏ quà Tết, bánh kẹo, rượu vang, mứt Tết.

            PHONG CÁCH TRẢ LỜI:
            - Trả lời ngắn gọn, tối đa 4-5 câu cho câu hỏi đơn giản
            - Gọi khách là "anh/chị", thân thiện, nhiệt tình
            - Sử dụng emoji phù hợp: 🎁 🌸 🎊 ✨ 🏮
            - Trả lời bằng tiếng Việt

            FORMAT KHI GỢI Ý SẢN PHẨM:
            🎁 **Tên sản phẩm** - Giá: xxx VNĐ
               Mô tả ngắn 1 dòng

            QUY TẮC QUAN TRỌNG:
            - KHÔNG bịa thông tin sản phẩm, giá, tồn kho
            - CHỈ dùng thông tin từ CONTEXT được cung cấp
            - Nếu không có thông tin → nói rõ + gợi ý liên hệ hotline
            - Tối đa 5 sản phẩm khi gợi ý

            LUÔN KẾT THÚC BẰNG:
            - Câu hỏi mở để tiếp tục tư vấn, HOẶC
            - Gợi ý hành động ("Anh/chị muốn em thêm vào giỏ hàng không ạ?")

            THÔNG TIN CỬA HÀNG:
            - Hotline: 1900-1234
            - Website: shophuypro.store
            - Giao hàng: Toàn quốc
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

            // 5. Generate response using LLM (with conversation history)
            String response = generateResponse(session, request.getMessage(), context, intent);

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

                // Add sorting info if present
                if (intent.getSortBy() != null) {
                    String sortLabel = switch (intent.getSortBy()) {
                        case "PRICE_DESC" -> "GIÁ CAO NHẤT → THẤP NHẤT (sản phẩm đầu tiên là đắt nhất)";
                        case "PRICE_ASC" -> "GIÁ THẤP NHẤT → CAO NHẤT (sản phẩm đầu tiên là rẻ nhất)";
                        case "NEWEST" -> "MỚI NHẤT";
                        default -> "";
                    };
                    context.append("📊 SẮP XẾP: ").append(sortLabel).append("\n\n");
                }

                // Add budget constraint info if present
                if (intent.getMaxPrice() != null) {
                    context.append("⚠️ NGÂN SÁCH KHÁCH HÀNG: tối đa ").append(intent.getMaxPrice()).append(" VNĐ\n");
                    context.append("→ CHỈ gợi ý sản phẩm có giá <= ").append(intent.getMaxPrice()).append(" VNĐ\n\n");
                }
                if (intent.getMinPrice() != null) {
                    context.append("Giá tối thiểu: ").append(intent.getMinPrice()).append(" VNĐ\n\n");
                }

                context.append("=== SẢN PHẨM TÌM THẤY ===\n");

                if (!products.isEmpty()) {
                    context.append("\nKết quả phù hợp:\n");
                    for (ProductEntity p : products) {
                        context.append(formatProduct(p)).append("\n");
                    }
                }

                // Filter semantic results by price if budget is set
                if (!semanticResults.isEmpty()) {
                    List<Document> filteredSemantic = semanticResults;
                    if (intent.getMaxPrice() != null) {
                        filteredSemantic = semanticResults.stream()
                                .filter(doc -> {
                                    Object priceObj = doc.getMetadata().get("price");
                                    if (priceObj != null) {
                                        try {
                                            return new java.math.BigDecimal(priceObj.toString())
                                                    .compareTo(intent.getMaxPrice()) <= 0;
                                        } catch (NumberFormatException e) {
                                            return false;
                                        }
                                    }
                                    return false;
                                })
                                .toList();
                    }
                    if (!filteredSemantic.isEmpty()) {
                        context.append("\nKết quả tìm kiếm liên quan:\n");
                        for (Document doc : filteredSemantic) {
                            context.append(doc.getText()).append("\n---\n");
                        }
                    }
                }

                if (products.isEmpty()) {
                    context.append("Không tìm thấy sản phẩm phù hợp với ngân sách/yêu cầu.\n");
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

                // Add active discounts (query only active + not expired)
                var activeDiscounts = discountRepository.findActiveAndNotExpired(PageRequest.of(0, 5));
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
        // Handle sortBy first - superlative queries like "đắt nhất", "rẻ nhất"
        if (intent.getSortBy() != null) {
            return switch (intent.getSortBy()) {
                case "PRICE_DESC" -> productRepository.findByPriceDesc(PageRequest.of(0, 10));
                case "PRICE_ASC" -> productRepository.findByPriceAsc(PageRequest.of(0, 10));
                case "NEWEST" -> productRepository.findTopActiveProducts(PageRequest.of(0, 10));
                default -> productRepository.findTopActiveProducts(PageRequest.of(0, 10));
            };
        }

        boolean hasCategory = intent.getCategory() != null;
        boolean hasKeyword = intent.getKeyword() != null;
        boolean hasMaxPrice = intent.getMaxPrice() != null;
        boolean hasMinPrice = intent.getMinPrice() != null;
        boolean hasPriceRange = hasMaxPrice && hasMinPrice;

        // === Combined filters: category/keyword + price ===

        // Category + price range
        if (hasCategory && hasPriceRange) {
            return productRepository.findByCategoryAndPriceRange(
                    intent.getCategory(), intent.getMinPrice(), intent.getMaxPrice());
        }

        // Category + max price
        if (hasCategory && hasMaxPrice) {
            return productRepository.findByCategoryAndMaxPrice(
                    intent.getCategory(), intent.getMaxPrice());
        }

        // Keyword + price range
        if (hasKeyword && hasPriceRange) {
            return productRepository.findByKeywordAndPriceRange(
                    intent.getKeyword(), intent.getMinPrice(), intent.getMaxPrice());
        }

        // Keyword + max price
        if (hasKeyword && hasMaxPrice) {
            return productRepository.findByKeywordAndMaxPrice(
                    intent.getKeyword(), intent.getMaxPrice());
        }

        // === Single filters ===

        if (hasPriceRange) {
            return productRepository.findByPriceBetween(intent.getMinPrice(), intent.getMaxPrice());
        }

        if (hasMaxPrice && intent.getQuantity() != null) {
            return productRepository.findByPriceAndAvailability(intent.getMaxPrice(), intent.getQuantity());
        }

        if (hasMaxPrice) {
            return productRepository.findByPriceLessThanEqual(intent.getMaxPrice());
        }

        if (hasCategory) {
            return productRepository.findByCategoryNameContaining(intent.getCategory());
        }

        if (hasKeyword) {
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
                String imageUrl = p.getImage(); // fallback to main image
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
                        .imageUrl(b.getImage())
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

    private String generateResponse(ChatSessionEntity session, String userMessage, String context,
            IntentResult intent) {
        ChatClient chatClient = chatClientBuilder.build();

        // Build conversation history (last 10 messages for context)
        String historyText = buildConversationHistory(session);

        String augmentedPrompt = String.format(
                """
                        %s

                        === LỊCH SỬ TRÒ CHUYỆN ===
                        %s

                        === THÔNG TIN SẢN PHẨM/DỊCH VỤ ===
                        %s

                        === CÂU HỎI MỚI NHẤT CỦA KHÁCH ===
                        %s

                        Hãy trả lời dựa trên thông tin ở trên. Nếu lịch sử trò chuyện có liên quan, hãy liên kết với câu trả lời.
                        ⚠️ TUYỆT ĐỐI KHÔNG gợi ý sản phẩm có giá CAO HƠN ngân sách khách hàng. Nếu không có sản phẩm phù hợp ngân sách, hãy nói rõ và gợi ý mức giá gần nhất.
                        Nếu không có thông tin, hãy nói rõ và gợi ý liên hệ hotline 1900-1234.
                        """,
                SYSTEM_PROMPT, historyText, context, userMessage);

        return chatClient.prompt()
                .user(augmentedPrompt)
                .call()
                .content();
    }

    private String buildConversationHistory(ChatSessionEntity session) {
        List<ChatMessageEntity> recentMessages = chatMessageRepository
                .findTop10BySessionIdOrderByCreatedAtDesc(session.getId());

        if (recentMessages.isEmpty()) {
            return "(Đây là tin nhắn đầu tiên)";
        }

        // Reverse to chronological order
        List<ChatMessageEntity> chronological = new ArrayList<>(recentMessages);
        Collections.reverse(chronological);

        return chronological.stream()
                .map(m -> {
                    String role = m.getRole() == ChatMessageEntity.MessageRole.USER ? "Khách" : "Tư vấn viên";
                    return role + ": " + m.getContent();
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Get conversation history for a session
     */
    public List<ChatMessageResponse> getHistory(String sessionToken) {
        return chatSessionRepository.findBySessionToken(sessionToken)
                .map(session -> chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()))
                .orElse(List.of())
                .stream()
                .map(m -> ChatMessageResponse.builder()
                        .id(m.getId())
                        .role(m.getRole().name())
                        .content(m.getContent())
                        .intent(m.getIntent())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
