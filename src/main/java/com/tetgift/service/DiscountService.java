package com.tetgift.service;

import com.tetgift.dto.request.DiscountRequest;
import com.tetgift.dto.response.DiscountResponse;

import java.util.List;

public interface DiscountService {
    DiscountResponse createDiscount(DiscountRequest request);

    List<DiscountResponse> getAllDiscounts();

    DiscountResponse getDiscountById(Long id);

    DiscountResponse updateDiscount(Long id, DiscountRequest request);

    void deleteDiscount(Long id);

    DiscountResponse validateDiscountCode(String code);
}
