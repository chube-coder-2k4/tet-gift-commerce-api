package com.tetgift.service.impl;

import com.tetgift.dto.request.DiscountRequest;
import com.tetgift.dto.response.DiscountResponse;
import com.tetgift.enums.DiscountType;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.entity.DiscountEntity;
import com.tetgift.repository.jpa.DiscountRepository;
import com.tetgift.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
                .discountType(request.getDiscountType()) // Lưu type
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount()) // Lưu max cap
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
    public DiscountResponse validateDiscountCode(String code, BigDecimal orderAmount) {
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

        // GỌI HÀM TẠI ĐÂY:
        BigDecimal actualDiscount = calculateActualDiscount(discount, orderAmount);

        if (actualDiscount.compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidDataException("Đơn hàng chưa đủ giá trị tối thiểu " + discount.getMinOrderAmount());
        }

        DiscountResponse response = toResponse(discount);
        // Bạn nên thêm field này vào DiscountResponse để trả về FE
        response.setActualDiscountAmount(actualDiscount);

        return response;
    }


    public BigDecimal calculateActualDiscount(DiscountEntity discount, BigDecimal orderAmount) {
        // 1. Kiểm tra đơn hàng tối thiểu
        if (orderAmount.compareTo(discount.getMinOrderAmount()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal actualDiscount = BigDecimal.ZERO;

        if (discount.getDiscountType() == DiscountType.FIXED) {
            // Ví dụ: Giảm thẳng 80k
            actualDiscount = discount.getDiscountValue();
        } else if(discount.getDiscountType() == DiscountType.PERCENTAGE) {
            // Ví dụ: Giảm 25% tối đa 80k
            // Tính % thực tế: (OrderAmount * Value) / 100
            actualDiscount = orderAmount.multiply(discount.getDiscountValue())
                    .divide(new BigDecimal(100));

            // Nếu có mức giảm tối đa, thì lấy giá trị nhỏ hơn
            if (discount.getMaxDiscountAmount() != null) {
                actualDiscount = actualDiscount.min(discount.getMaxDiscountAmount());
            }
        }
        return actualDiscount;
    }

    private DiscountResponse toResponse(DiscountEntity discount) {
        String display = discount.getDiscountType() == DiscountType.PERCENTAGE
                ? discount.getDiscountValue() + "%"
                : discount.getDiscountValue() + "đ";

        String typeLabel = discount.getDiscountType() == DiscountType.PERCENTAGE
                ? "Giảm " + discount.getDiscountValue().stripTrailingZeros().toPlainString() + "%"
                : "Giảm tiền mặt";

        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .discountType(discount.getDiscountType().name())   // Trả về type dưới dạng String
                .discountValue(discount.getDiscountValue())
                .maxDiscountAmount(discount.getMaxDiscountAmount()) // Trả về max cap\
                .getDisplayValue(display)
                .typeLable(typeLabel)
                .minOrderAmount(discount.getMinOrderAmount())
                .usageLimit(discount.getUsageLimit())
                .usageCount(discount.getUsageCount())
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .isActive(discount.isActive())
                .build();
    }
}
