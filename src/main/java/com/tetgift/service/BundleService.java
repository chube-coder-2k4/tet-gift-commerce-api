package com.tetgift.service;

import com.tetgift.dto.request.BundleRequest;
import com.tetgift.dto.response.BundleResponse;
import com.tetgift.dto.response.PageResponse;

public interface BundleService {
    Long createBundle(BundleRequest request);

    BundleResponse getBundleById(Long id);

    PageResponse<BundleResponse> getAllBundles(int page, int size, String sortBy, String sortDir);

    Long updateBundle(Long id, BundleRequest request);

    void deleteBundle(Long id);
}
