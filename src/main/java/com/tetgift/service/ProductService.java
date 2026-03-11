package com.tetgift.service;

import com.tetgift.dto.request.ProductRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductResponse;

public interface ProductService {
    Long saveProduct(ProductRequest productRequest);

    ProductResponse findProductById(Long id);

    PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);

    Long updateProduct(Long id, ProductRequest productRequest);

    void deleteProduct(Long id);
}
