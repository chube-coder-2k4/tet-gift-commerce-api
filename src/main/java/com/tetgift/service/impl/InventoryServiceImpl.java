package com.tetgift.service.impl;

import com.tetgift.dto.response.InventoryBatchResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.model.entity.InventoryBatchEntity;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.repository.jpa.InventoryBatchRepository;
import com.tetgift.repository.jpa.ProductRepository;
import com.tetgift.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final InventoryBatchRepository batchRepository;

    private final ProductRepository productRepository;

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
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());


        Page<InventoryBatchEntity> batchPage = batchRepository.findByProductId(productId, pageable);


        List<InventoryBatchResponse> data = batchPage.getContent().stream()
                .map(this::mapToBatchResponse)
                .toList();


        return PageResponse.<InventoryBatchResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalItems(batchPage.getTotalElements())
                .totalPages(batchPage.getTotalPages())
                .data(data)
                .build();
    }

    @Override
    public void disposeBatch(Long batchId) {
        InventoryBatchEntity batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lô hàng với ID: " + batchId));

        int quantityToDispose = batch.getCurrentQuantity();

        if (quantityToDispose <= 0) {
            throw new RuntimeException("Lô hàng này đã hết hoặc đã được xuất hủy trước đó.");
        }

        ProductEntity product = batch.getProduct();
        if (product == null) {
            throw new RuntimeException("Không tìm thấy thông tin sản phẩm của lô hàng này.");
        }

        int newStock = product.getStock() - quantityToDispose;
        product.setStock(Math.max(0, newStock));

        batch.setCurrentQuantity(0);

        productRepository.save(product);
        batchRepository.save(batch);

        log.info("Đã xuất hủy lô hàng {} (ID: {}). Trừ {} sản phẩm khỏi tổng tồn kho.",
                batch.getBatchCode(), batchId, quantityToDispose);
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