package com.tetgift.service.impl;

import com.tetgift.dto.response.InventoryBatchResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.model.entity.InventoryBatchEntity;
import com.tetgift.repository.jpa.InventoryBatchRepository;
import com.tetgift.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryBatchRepository batchRepository;

    public PageResponse<InventoryBatchResponse> getAllBatches(int page, int size) {




        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        Page<InventoryBatchEntity> batches = batchRepository.findAllWithProduct(pageable);
//        if (keyword != null && !keyword.isEmpty()) {
//            batches = batchRepository.searchBatches(keyword, pageable);
//        } else {
//            batches = batchRepository.findAllWithProduct(pageable);
//        }
        List<InventoryBatchResponse> data = batches.getContent().stream()
                .map(b -> InventoryBatchResponse.builder()
                        .id(b.getId())
                        .batchCode(b.getBatchCode())
                        .productId(b.getProduct().getId())
                        .productName(b.getProduct().getName())
                        .importQuantity(b.getImportQuantity())
                        .currentQuantity(b.getCurrentQuantity())
                        .importPrice(b.getImportPrice())
                        .manufactureDate(b.getManufactureDate())
                        .expiryDate(b.getExpiryDate())
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();

        return PageResponse.<InventoryBatchResponse>builder()
                .data(data)
                .pageNo(page)
                .pageSize(size)
                .totalItems(batches.getTotalElements())
                .totalPages(batches.getTotalPages())
                .build();
    }

    @Override
    public PageResponse<InventoryBatchResponse> getBatchesByProduct(Long productId, int page, int size) {
        // 1. Tạo đối tượng phân trang, sắp xếp theo hạn sử dụng tăng dần (ASC)
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());

        // 2. Thực hiện truy vấn từ Repository
        // Lưu ý: Bạn nên viết hàm findByProductId trong InventoryBatchRepository trả về Page
        Page<InventoryBatchEntity> batchPage = batchRepository.findByProductId(productId, pageable);

        // 3. Chuyển đổi từ Entity sang DTO
        List<InventoryBatchResponse> data = batchPage.getContent().stream()
                .map(this::mapToBatchResponse) // Sử dụng một hàm helper để map dữ liệu
                .toList();

        // 4. Trả về đối tượng PageResponse theo cấu trúc của bạn
        return PageResponse.<InventoryBatchResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalItems(batchPage.getTotalElements())
                .totalPages(batchPage.getTotalPages())
                .data(data)
                .build();
    }



    // Hàm Helper để tái sử dụng logic Mapping
    private InventoryBatchResponse mapToBatchResponse(InventoryBatchEntity b) {
        return InventoryBatchResponse.builder()
                .id(b.getId())
                .batchCode(b.getBatchCode())
                .productId(b.getProduct().getId())
                .productName(b.getProduct().getName())
                .importQuantity(b.getImportQuantity())
                .currentQuantity(b.getCurrentQuantity())
                .importPrice(b.getImportPrice())
                .manufactureDate(b.getManufactureDate())
                .expiryDate(b.getExpiryDate())
                .createdAt(b.getCreatedAt())
                .build();
    }
}