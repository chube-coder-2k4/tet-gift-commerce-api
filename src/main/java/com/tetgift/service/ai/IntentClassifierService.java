package com.tetgift.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetgift.dto.response.IntentResult;
import com.tetgift.dto.response.IntentResult.IntentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for classifying user intent using LLM
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "true", matchIfMissing = true)
public class IntentClassifierService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    private static final String INTENT_CLASSIFICATION_PROMPT = """
            Bạn là AI phân loại intent cho shop quà Tết. Phân tích câu hỏi và trả về JSON:

            {
                "intent": "PRODUCT_SEARCH | BUNDLE_SEARCH | CATEGORY_BROWSE | STOCK_CHECK | DISCOUNT_POLICY | SHOP_INFO | GENERAL_CHAT",
                "maxPrice": số tiền tối đa (VNĐ, null nếu không có),
                "minPrice": số tiền tối thiểu (VNĐ, null nếu không có),
                "category": tên danh mục (null nếu không có),
                "quantity": số lượng cần mua (null nếu không có),
                "keyword": từ khóa sản phẩm chính (null nếu không có),
                "sortBy": "PRICE_DESC | PRICE_ASC | NEWEST | POPULAR | null"
            }

            Quy tắc phân loại intent:
            - PRODUCT_SEARCH: Tìm sản phẩm, hỏi về sản phẩm cụ thể, so sánh giá, hỏi giá sản phẩm
            - BUNDLE_SEARCH: Tìm combo, giỏ quà, set quà
            - CATEGORY_BROWSE: Duyệt theo danh mục (bánh, mứt, rượu, trà...)
            - STOCK_CHECK: Hỏi còn hàng không, số lượng tồn kho
            - DISCOUNT_POLICY: Hỏi giảm giá, khuyến mãi, mua nhiều
            - SHOP_INFO: Hỏi địa chỉ, liên hệ, giờ mở cửa
            - GENERAL_CHAT: Các câu hỏi chung khác

            ⚠️ QUY TẮC EXTRACT keyword VÀ category (RẤT QUAN TRỌNG):
            - LUÔN extract keyword khi user đề cập loại sản phẩm cụ thể
            - "rượu", "rượu vang" → keyword: "rượu"
            - "trà", "trà sen" → keyword: "trà"
            - "bánh", "bánh chưng" → keyword: "bánh"
            - "mứt" → keyword: "mứt"
            - "hạt", "hạt điều" → keyword: "hạt"
            - "kẹo" → keyword: "kẹo"
            - "cà phê" → keyword: "cà phê"
            - Nếu user hỏi "rượu tầm 300k" → keyword: "rượu", maxPrice: 300000
            - Nếu user hỏi "trà dưới 200k" → keyword: "trà", maxPrice: 200000
            - "tầm X", "khoảng X" → maxPrice: X (cho phép chênh lệch 20%%)

            Quy tắc sortBy:
            - "đắt nhất", "cao nhất", "premium", "sang nhất" → PRICE_DESC
            - "rẻ nhất", "thấp nhất", "tiết kiệm", "giá tốt" → PRICE_ASC
            - "mới nhất", "mới ra", "vừa ra" → NEWEST
            - "bán chạy", "phổ biến", "hot", "được mua nhiều" → POPULAR
            - Nếu không đề cập → null

            Chỉ trả về JSON, không có text khác.

            Câu hỏi: %s
            """;

    public IntentResult classifyIntent(String userMessage) {
        log.info("Classifying intent for: {}", userMessage);

        try {
            ChatClient chatClient = chatClientBuilder.build();

            String response = chatClient.prompt()
                    .user(String.format(INTENT_CLASSIFICATION_PROMPT, userMessage))
                    .call()
                    .content();

            log.debug("Intent classification response: {}", response);

            return parseIntentResponse(response, userMessage);

        } catch (Exception e) {
            log.error("Error classifying intent, defaulting to GENERAL_CHAT", e);
            return IntentResult.builder()
                    .intent(IntentType.GENERAL_CHAT)
                    .rawQuery(userMessage)
                    .build();
        }
    }

    private IntentResult parseIntentResponse(String response, String originalQuery) {
        try {
            // Clean response - remove markdown code blocks if present
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.startsWith("```")) {
                cleanResponse = cleanResponse.substring(3);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            JsonNode jsonNode = objectMapper.readTree(cleanResponse);

            IntentResult.IntentResultBuilder builder = IntentResult.builder()
                    .rawQuery(originalQuery);

            // Parse intent
            String intentStr = jsonNode.has("intent") ? jsonNode.get("intent").asText() : "GENERAL_CHAT";
            try {
                builder.intent(IntentType.valueOf(intentStr));
            } catch (IllegalArgumentException e) {
                builder.intent(IntentType.GENERAL_CHAT);
            }

            // Parse optional fields
            if (jsonNode.has("maxPrice") && !jsonNode.get("maxPrice").isNull()) {
                String priceStr = jsonNode.get("maxPrice").asText();
                builder.maxPrice(new BigDecimal(priceStr.replaceAll("[^0-9.]", "")));
            }

            if (jsonNode.has("minPrice") && !jsonNode.get("minPrice").isNull()) {
                String priceStr = jsonNode.get("minPrice").asText();
                builder.minPrice(new BigDecimal(priceStr.replaceAll("[^0-9.]", "")));
            }

            if (jsonNode.has("category") && !jsonNode.get("category").isNull()
                    && !"null".equals(jsonNode.get("category").asText())) {
                builder.category(jsonNode.get("category").asText());
            }

            if (jsonNode.has("quantity") && !jsonNode.get("quantity").isNull()) {
                builder.quantity(jsonNode.get("quantity").asInt());
            }

            if (jsonNode.has("keyword") && !jsonNode.get("keyword").isNull()
                    && !"null".equals(jsonNode.get("keyword").asText())) {
                builder.keyword(jsonNode.get("keyword").asText());
            }

            // Parse sortBy
            if (jsonNode.has("sortBy") && !jsonNode.get("sortBy").isNull()
                    && !"null".equals(jsonNode.get("sortBy").asText())) {
                builder.sortBy(jsonNode.get("sortBy").asText());
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Error parsing intent response: {}", response, e);
            return IntentResult.builder()
                    .intent(IntentType.GENERAL_CHAT)
                    .rawQuery(originalQuery)
                    .build();
        }
    }
}
