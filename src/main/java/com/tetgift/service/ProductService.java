package com.tetgift.service;

import com.tetgift.dto.request.ProductRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    Long saveProduct(ProductRequest productRequest);
    Long saveProduct(ProductRequest productRequest, MultipartFile image) throws java.io.IOException;

    ProductResponse findProductById(Long id);

    PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);

    Long updateProduct(Long id, ProductRequest productRequest);
    Long updateProduct(Long id, ProductRequest productRequest, MultipartFile image) throws java.io.IOException;

    void deleteProduct(Long id);
}
