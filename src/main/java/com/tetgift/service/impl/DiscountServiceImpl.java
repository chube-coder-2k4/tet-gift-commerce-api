package com.tetgift.service.impl;

import com.tetgift.dto.request.DiscountRequest;
import com.tetgift.dto.response.DiscountResponse;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.entity.DiscountEntity;
import com.tetgift.repository.jpa.DiscountRepository;
import com.tetgift.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    @Override
    @Transactional
    public DiscountResponse createDiscount(DiscountRequest request) {
        DiscountEntity discount = DiscountEntity.builder()
                .code(request.getCode().toUpperCase())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .usageLimit(request.getUsageLimit())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .build();
        DiscountEntity saved = discountRepository.save(discount);
        return toResponse(saved);
    }

    @Override
    public List<DiscountResponse> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(this::toResponse).toList();
    }

    @Override
    public DiscountResponse getDiscountById(Long id) {
        DiscountEntity discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
        return toResponse(discount);
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(Long id, DiscountRequest request) {
        DiscountEntity discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
        discount.setCode(request.getCode().toUpperCase());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setMinOrderAmount(request.getMinOrderAmount());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        DiscountEntity updated = discountRepository.save(discount);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id) {
        DiscountEntity discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
        discount.setActive(false);
        discountRepository.save(discount);
    }

    @Override
    public DiscountResponse validateDiscountCode(String code) {
        DiscountEntity discount = discountRepository.findByCodeAndIsActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Discount code not found or inactive"));

        LocalDateTime now = LocalDateTime.now();
        if (discount.getStartDate() != null && now.isBefore(discount.getStartDate())) {
            throw new InvalidDataException("Discount code is not yet active");
        }
        if (discount.getEndDate() != null && now.isAfter(discount.getEndDate())) {
            throw new InvalidDataException("Discount code has expired");
        }
        if (discount.getUsageLimit() != null && discount.getUsageCount() >= discount.getUsageLimit()) {
            throw new InvalidDataException("Discount code has reached its usage limit");
        }

        return toResponse(discount);
    }

    private DiscountResponse toResponse(DiscountEntity discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .discountValue(discount.getDiscountValue())
                .minOrderAmount(discount.getMinOrderAmount())
                .usageLimit(discount.getUsageLimit())
                .usageCount(discount.getUsageCount())
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .isActive(discount.isActive())
                .build();
    }
}
