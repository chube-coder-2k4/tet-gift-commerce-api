package com.tetgift.service;

import com.tetgift.dto.response.InventoryBatchResponse;
import com.tetgift.dto.response.PageResponse;

public interface InventoryService {
    PageResponse<InventoryBatchResponse> getAllBatches(int page, int size);

    PageResponse<InventoryBatchResponse> getBatchesByProduct(Long productId, int page, int size);

    void disposeBatch(Long batchId);
}
