package com.tetgift.service.impl;

import com.tetgift.dto.request.BundleRequest;
import com.tetgift.dto.response.BundleProductResponse;
import com.tetgift.dto.response.BundleResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductImageResponse;
import com.tetgift.exception.ProductNotFoundException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.entity.BundleEntity;
import com.tetgift.model.entity.BundleProductEntity;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.repository.jpa.BundleRepository;
import com.tetgift.repository.jpa.ProductRepository;
import com.tetgift.mapper.BundleMapper;
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
import java.math.BigDecimal;
import java.util.Collections;
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
            
            // Link back bundle and replace detached products with managed products
            if (bundle.getBundleProducts() != null) {
                for (BundleProductEntity bundleProduct : bundle.getBundleProducts()) {
                    bundleProduct.setBundle(bundle); // Link back
                    if (bundleProduct.getProduct() != null && bundleProduct.getProduct().getId() != null) {
                        ProductEntity product = productRepository.findById(bundleProduct.getProduct().getId())
                                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + bundleProduct.getProduct().getId()));
                        bundleProduct.setProduct(product); // Replace detached product with fetched one
                    }
                }
            }

            // Calculate total price if not custom
            if (!request.isCustom() && bundle.getBundleProducts() != null) {
                BigDecimal totalPrice = bundle.getBundleProducts().stream()
                        .map(bp -> bp.getProduct().getPrice().multiply(BigDecimal.valueOf(bp.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                bundle.setPrice(totalPrice);
            }

            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(image, "bundles");
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

            bundleMapper.updateEntity(bundle, request);

            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(image, "bundles");
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

                // recalculate total price
                if (!request.isCustom()) {
                    BigDecimal totalPrice = bundleProducts.stream()
                        .map(bp -> bp.getProduct().getPrice().multiply(BigDecimal.valueOf(bp.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    bundle.setPrice(totalPrice);
                }
            }

            BundleEntity updated = bundleRepository.save(bundle);
            return updated.getId();
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
                .map(bp -> {
                    ProductEntity product = bp.getProduct();
                    List<ProductImageResponse> images = Collections.emptyList();
                    if (product != null && product.getProductImages() != null) {
                        images = product.getProductImages().stream()
                                .map(img -> ProductImageResponse.builder()
                                        .id(img.getId())
                                        .imageUrl(img.getImageUrl())
                                        .imageType(img.getImageType())
                                        .publicId(img.getPublicId())
                                        .isPrimary(img.isPrimary())
                                        .build())
                                .toList();
                    }

                    return BundleProductResponse.builder()
                            .id(bp.getId())
                            .productId(product != null ? product.getId() : null)
                            .productName(product != null ? product.getName() : null)
                            .productPrice(product != null ? product.getPrice() : null)
                            .quantity(bp.getQuantity())
                            .images(images)
                            .build();
                }).toList();

        return BundleResponse.builder()
                .id(bundle.getId())
                .name(bundle.getName())
                .description(bundle.getDescription())
                .price(bundle.getPrice())
                .image(bundle.getImage())
                .isCustom(bundle.isCustom())
                .isActive(bundle.isActive())
                .products(products)
                .build();
    }
}
