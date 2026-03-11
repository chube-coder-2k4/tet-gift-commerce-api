package com.tetgift.service.impl;

import com.tetgift.dto.request.BundleRequest;
import com.tetgift.dto.response.BundleProductResponse;
import com.tetgift.dto.response.BundleResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.exception.ProductNotFoundException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.entity.BundleEntity;
import com.tetgift.model.entity.BundleProductEntity;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.repository.jpa.BundleRepository;
import com.tetgift.repository.jpa.ProductRepository;
import com.tetgift.service.BundleService;
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
public class BundleServiceImpl implements BundleService {

    private final BundleRepository bundleRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Long createBundle(BundleRequest request) {
        BundleEntity bundle = BundleEntity.builder()
                .name(request.getName())
                .price(request.getPrice())
                .isCustom(request.isCustom())
                .isActive(true)
                .build();

        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            List<BundleProductEntity> bundleProducts = request.getProducts().stream()
                    .map(bp -> {
                        ProductEntity product = productRepository.findByIdAndIsActiveTrue(bp.getProductId())
                                .orElseThrow(
                                        () -> new ProductNotFoundException("Product not found: " + bp.getProductId()));
                        return BundleProductEntity.builder()
                                .bundle(bundle)
                                .product(product)
                                .quantity(bp.getQuantity() != null ? bp.getQuantity() : 1)
                                .build();
                    }).toList();
            bundle.setBundleProducts(new ArrayList<>(bundleProducts));
        }

        BundleEntity saved = bundleRepository.save(bundle);
        return saved.getId();
    }

    @Override
    public BundleResponse getBundleById(Long id) {
        BundleEntity bundle = bundleRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found: " + id));
        return toResponse(bundle);
    }

    @Override
    public PageResponse<BundleResponse> getAllBundles(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(Math.max(page, 0), size, sort);
        Page<BundleEntity> bundles = bundleRepository.findByIsActiveTrue(pageable);

        List<BundleResponse> responses = bundles.getContent().stream()
                .map(this::toResponse).toList();

        return PageResponse.<BundleResponse>builder()
                .data(responses)
                .pageNo(page)
                .pageSize(size)
                .totalItems(bundles.getTotalElements())
                .totalPages(bundles.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public Long updateBundle(Long id, BundleRequest request) {
        BundleEntity bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found: " + id));

        bundle.setName(request.getName());
        bundle.setPrice(request.getPrice());
        bundle.setCustom(request.isCustom());

        if (request.getProducts() != null) {
            bundle.getBundleProducts().clear();
            List<BundleProductEntity> bundleProducts = request.getProducts().stream()
                    .map(bp -> {
                        ProductEntity product = productRepository.findByIdAndIsActiveTrue(bp.getProductId())
                                .orElseThrow(
                                        () -> new ProductNotFoundException("Product not found: " + bp.getProductId()));
                        return BundleProductEntity.builder()
                                .bundle(bundle)
                                .product(product)
                                .quantity(bp.getQuantity() != null ? bp.getQuantity() : 1)
                                .build();
                    }).toList();
            bundle.getBundleProducts().addAll(bundleProducts);
        }

        BundleEntity updated = bundleRepository.save(bundle);
        return updated.getId();
    }

    @Override
    @Transactional
    public void deleteBundle(Long id) {
        BundleEntity bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle not found: " + id));
        bundle.setActive(false);
        bundleRepository.save(bundle);
    }

    private BundleResponse toResponse(BundleEntity bundle) {
        List<BundleProductResponse> products = bundle.getBundleProducts().stream()
                .map(bp -> BundleProductResponse.builder()
                        .id(bp.getId())
                        .productId(bp.getProduct().getId())
                        .productName(bp.getProduct().getName())
                        .productPrice(bp.getProduct().getPrice())
                        .quantity(bp.getQuantity())
                        .build())
                .toList();

        return BundleResponse.builder()
                .id(bundle.getId())
                .name(bundle.getName())
                .price(bundle.getPrice())
                .isCustom(bundle.isCustom())
                .isActive(bundle.isActive())
                .products(products)
                .build();
    }
}
