package com.tetgift.service.impl;

import com.tetgift.dto.request.ProductRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductResponse;
import com.tetgift.exception.CategoryNotFoundException;
import com.tetgift.exception.ProductNotFoundException;
import com.tetgift.mapper.ProductMapper;
import com.tetgift.model.Category;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.model.entity.ProductImageEntity;
import com.tetgift.repository.jpa.CategoryRepository;
import com.tetgift.repository.jpa.ProductRepository;
import com.tetgift.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse findProductById(Long id) {
        ProductEntity product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public Long saveProduct(ProductRequest request) {
        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .isActive(true)
                .manufactureDate(request.getManufactureDate())
                .expDate(request.getExpDate())
                .build();

        // Set category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndIsActiveTrue(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Set images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImageEntity> images = request.getImages().stream()
                    .map(imgReq -> ProductImageEntity.builder()
                            .imageUrl(imgReq.getImageUrl())
                            .imageType(imgReq.getImageType())
                            .publicId(imgReq.getPublicId())
                            .isPrimary(imgReq.isPrimary())
                            .product(product)
                            .build())
                    .toList();
            product.setProductImages(new ArrayList<>(images));
        }

        ProductEntity saved = productRepository.save(product);
        return saved.getId();
    }

    @Override
    @Transactional
    public Long updateProduct(Long id, ProductRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() != null ? request.getStock() : product.getStock());
        product.setManufactureDate(request.getManufactureDate());
        product.setExpDate(request.getExpDate());

        // Update category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndIsActiveTrue(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Update images (replace all)
        if (request.getImages() != null) {
            product.getProductImages().clear();
            List<ProductImageEntity> images = request.getImages().stream()
                    .map(imgReq -> ProductImageEntity.builder()
                            .imageUrl(imgReq.getImageUrl())
                            .imageType(imgReq.getImageType())
                            .publicId(imgReq.getPublicId())
                            .isPrimary(imgReq.isPrimary())
                            .product(product)
                            .build())
                    .toList();
            product.getProductImages().addAll(images);
        }

        ProductEntity updated = productRepository.save(product);
        return updated.getId();
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        if (!product.isActive()) {
            throw new ProductNotFoundException("Product already deleted with id: " + id);
        }

        // Soft delete
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(Math.max(page, 0), size, sort);
        Page<ProductEntity> products = productRepository.findByIsActiveTrue(pageable);
        List<ProductResponse> responses = productMapper.toResponseList(products.getContent());

        return PageResponse.<ProductResponse>builder()
                .data(responses)
                .pageNo(page)
                .pageSize(size)
                .totalItems(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build();
    }
}
