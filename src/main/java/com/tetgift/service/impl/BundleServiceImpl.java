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
import com.tetgift.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BundleServiceImpl implements BundleService {
    private final BundleRepository bundleRepository;
    private final ProductRepository productRepository;
    private final BundleMapper bundleMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public Long createBundle(BundleRequest request) {
        return createBundleInternal(request, null);
    }

    @Override
    @Transactional
    public Long createBundle(BundleRequest request, MultipartFile image) {
        return createBundleInternal(request, image);
    }

    private Long createBundleInternal(BundleRequest request, MultipartFile image) {
        try {
            BundleEntity bundle = bundleMapper.toEntity(request);
            // Calculate total price if not custom
            if (!request.isCustom()) {
                List<ProductEntity> products = new ArrayList<>();
                for (BundleProductEntity bundleProduct : bundle.getBundleProducts()) {
                    ProductEntity product = productRepository.findByIdAndIsActiveTrue(bundleProduct.getProduct().getId())
                            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + bundleProduct.getProduct().getId()));
                    products.add(product);
                }
                double totalPrice = products.stream().mapToDouble(ProductEntity::getPrice).sum();
                bundle.setPrice(totalPrice);
            }

            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(image);
                bundle.setImage(imageUrl);
            }

            BundleEntity saved = bundleRepository.save(bundle);
            return saved.getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
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
        return updateBundleInternal(id, request, null);
    }

    @Override
    @Transactional
    public Long updateBundle(Long id, BundleRequest request, MultipartFile image) {
        return updateBundleInternal(id, request, image);
    }

    private Long updateBundleInternal(Long id, BundleRequest request, MultipartFile image) {
        try {
            BundleEntity bundle = bundleRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Bundle not found"));

            bundleMapper.updateBundle(bundle, request);

            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(image);
                bundle.setImage(imageUrl);
            }

            // Update products logic ...
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

            return bundleRepository.save(bundle).getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
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
