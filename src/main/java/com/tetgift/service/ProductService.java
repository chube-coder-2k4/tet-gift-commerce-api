package com.tetgift.service;

import com.tetgift.dto.request.ProductRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductResponse;

import java.util.UUID;

public interface ProductService {
    ProductResponse findProductById(Long id);
    Long saveProduct(ProductRequest productRequest);
    Long updateProduct(Long id, ProductRequest productRequest);
    void deleteProduct(Long id);
    PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);

}
