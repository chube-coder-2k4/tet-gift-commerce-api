package com.tetgift.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetgift.dto.response.ConversationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for Conversation State Tracking and Intent Extraction (Slot Filling)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "chatbot.enabled", havingValue = "true", matchIfMissing = true)
public class IntentParserService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        Bạn là hệ thống Xử lý Ngôn ngữ Tự nhiên bóc tách ý định (Intent) và biến số (Slot Filling) cho shop Quà Tết.
        Dựa vào Lịch sử (nếu có) và Câu nói hiện tại, trả kết quả DUY NHẤT ở định dạng JSON:
        
        {
            "intent": "RECOMMEND | NEED_INFO | GREETING | FAQ | GENERAL_CHAT",
            "maxBudget": <số tiền tối đa bằng SỐ (Ví dụ: 2000000), nếu không rõ thì null>,
            "recipient": "<sếp | đối tác | gia đình | người yêu>, null nếu không rõ",
            "category": "<giỏ quà | rượu vang | bánh kẹo>, null nếu không rõ",
            "readyToRecommend": <true nếu người dùng ĐÃ có đủ thông tin (ngân sách, đối tượng) hoặc yêu cầu gợi ý ngay, false nếu CẦN hỏi thêm>,
            "missingSlotPrompt": "<Câu hỏi ngắn gọn, lịch sự, có emoji gợi ý hỏi thêm ngân sách hoặc đối tượng để dễ tư vấn. null nếu readyToRecommend=true>"
        }
        
        QUY TẮC PHÂN LOẠI INTENT:
        - RECOMMEND: Tìm kiếm, mua sắm, cần gợi ý sản phẩm VÀ đã đủ thông tin.
        - NEED_INFO: Muốn mua nhưng CHƯA NÓI RÕ ngân sách (budget) hoặc đối tượng (recipient).
        - FAQ: Hỏi chính sách giao hàng, đổi trả, địa chỉ shop, sđt.
        - GREETING/GENERAL_CHAT: Xin chào, cám ơn, vâng, ừ.
        
        Lịch sử:
        %s
        
        Câu mới nhất (User):
        %s
        """;

    public ConversationState parseIntent(String history, String currentMessage) {
        log.info("Parsing intent for message: {}", currentMessage);
        try {
            ChatClient chatClient = chatClientBuilder.build();
            String prompt = String.format(SYSTEM_PROMPT, 
                    (history != null && !history.isBlank()) ? history : "Trống", 
                    currentMessage);
            
            String response = chatClient.prompt().user(prompt).call().content();
            return mapResponse(response);
        } catch (Exception e) {
            log.error("Intent parsing failed, defaulting to GENERAL_CHAT", e);
            return ConversationState.builder().intent("GENERAL_CHAT").readyToRecommend(true).build();
        }
    }

    private ConversationState mapResponse(String rawJson) {
        try {
            String cleanJson = rawJson.trim();
            if (cleanJson.startsWith("```json")) cleanJson = cleanJson.substring(7);
            if (cleanJson.startsWith("```")) cleanJson = cleanJson.substring(3);
            if (cleanJson.endsWith("```")) cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            cleanJson = cleanJson.trim();

            JsonNode node = objectMapper.readTree(cleanJson);
            ConversationState state = new ConversationState();
            state.setIntent(node.has("intent") ? node.get("intent").asText("GENERAL_CHAT") : "GENERAL_CHAT");
            
            if (node.has("maxBudget") && !node.get("maxBudget").isNull() && !node.get("maxBudget").asText().equalsIgnoreCase("null")) {
                state.setMaxBudget(new BigDecimal(node.get("maxBudget").asText().replaceAll("[^0-9]", "")));
            }
            if (node.has("recipient") && !node.get("recipient").isNull() && !node.get("recipient").asText().equalsIgnoreCase("null")) {
                state.setRecipient(node.get("recipient").asText());
            }
            if (node.has("category") && !node.get("category").isNull() && !node.get("category").asText().equalsIgnoreCase("null")) {
                state.setCategory(node.get("category").asText());
            }
            
            state.setReadyToRecommend(node.has("readyToRecommend") && node.get("readyToRecommend").asBoolean(false));
            
            if (node.has("missingSlotPrompt") && !node.get("missingSlotPrompt").isNull() && !node.get("missingSlotPrompt").asText().equalsIgnoreCase("null")) {
                state.setMissingSlotPrompt(node.get("missingSlotPrompt").asText());
            }
            log.info("Parsed Intent: {}", state.getIntent());
            return state;
        } catch (Exception e) {
            log.error("Failed to map json to ConversationState", e);
            return ConversationState.builder().intent("GENERAL_CHAT").readyToRecommend(true).build();
        }
    }
}
