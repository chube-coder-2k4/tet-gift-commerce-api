package com.tetgift.service;

import com.tetgift.dto.request.BundleRequest;
import com.tetgift.dto.response.BundleResponse;
import com.tetgift.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BundleService {
    Long createBundle(BundleRequest request);
    Long createBundle(BundleRequest request, MultipartFile image) throws java.io.IOException;

    BundleResponse getBundleById(Long id);

    PageResponse<BundleResponse> getAllBundles(int page, int size, String sortBy, String sortDir);

    Long updateBundle(Long id, BundleRequest request);
    Long updateBundle(Long id, BundleRequest request, MultipartFile image) throws java.io.IOException;

    void deleteBundle(Long id);
}
