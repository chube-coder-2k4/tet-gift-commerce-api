package com.tetgift.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Intent classification result from LLM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentResult {

    private IntentType intent;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private String category;
    private Integer quantity;
    private String keyword;
    private String rawQuery;

    public enum IntentType {
        PRODUCT_SEARCH,      // Tìm sản phẩm
        BUNDLE_SEARCH,       // Tìm combo/giỏ quà
        CATEGORY_BROWSE,     // Duyệt theo danh mục
        STOCK_CHECK,         // Kiểm tra tồn kho
        DISCOUNT_POLICY,     // Chính sách giảm giá
        SHOP_INFO,           // Thông tin cửa hàng
        GENERAL_CHAT         // Trò chuyện chung
    }
}

