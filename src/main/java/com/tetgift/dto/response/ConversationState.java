package com.tetgift.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationState {
    private String intent; // RECOMMEND, NEED_INFO, GREETING, FAQ, GENERAL_CHAT
    private BigDecimal maxBudget;
    private String recipient;
    private String category;
    private boolean readyToRecommend;
    private String missingSlotPrompt;
}
