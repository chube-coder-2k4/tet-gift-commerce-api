package com.tetgift.service.impl;

import com.tetgift.dto.request.ProductRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductResponse;
import com.tetgift.exception.CategoryNotFoundException;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ProductNotFoundException;
import com.tetgift.mapper.ProductMapper;
import com.tetgift.model.Category;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.model.entity.ProductImageEntity;
import com.tetgift.repository.jpa.CategoryRepository;
import com.tetgift.repository.jpa.ProductRepository;
import com.tetgift.service.CloudinaryService;
import com.tetgift.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    //hello
    @Override
    public ProductResponse findProductById(Long id) {
        ProductEntity product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public Long saveProduct(ProductRequest request) {
        return saveProductInternal(request, null);
    }

    @Override
    @Transactional
    public Long saveProduct(ProductRequest request, MultipartFile[] images) {
        return saveProductInternal(request, images);
    }

    private Long saveProductInternal(ProductRequest request, MultipartFile[] images) {
        try {
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

            // Handle JSON image requests (URLs passed directly)
            if (request.getImages() != null && !request.getImages().isEmpty()) {
                List<ProductImageEntity> imageEntities = request.getImages().stream()
                        .map(imgReq -> ProductImageEntity.builder()
                                .imageUrl(imgReq.getImageUrl())
                                .imageType(imgReq.getImageType())
                                .publicId(imgReq.getPublicId())
                                .isPrimary(imgReq.isPrimary())
                                .product(product)
                                .build())
                        .toList();
                product.setProductImages(new ArrayList<>(imageEntities));
            }

            // Handle multipart file uploads → upload to Cloudinary and create ProductImageEntity
            if (images != null && images.length > 0) {
                uploadAndAttachImages(product, images);
            }

            // Ensure at least one image is marked as primary
            ensureOnePrimaryImage(product);

            // Set the legacy `image` field to the primary image URL for backward compatibility
            syncPrimaryImageField(product);

            ProductEntity saved = productRepository.save(product);
            log.info("Product created with {} images, id={}", saved.getProductImages().size(), saved.getId());
            return saved.getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Override
    @Transactional
    public Long updateProduct(Long id, ProductRequest request) {
        return updateProductInternal(id, request, null);
    }

    @Override
    @Transactional
    public Long updateProduct(Long id, ProductRequest request, MultipartFile[] images) {
        return updateProductInternal(id, request, images);
    }

    private Long updateProductInternal(Long id, ProductRequest request, MultipartFile[] images) {
        try {
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

            // Update images from JSON request (replace all existing JSON-based images)
            if (request.getImages() != null) {
                // Delete old images from Cloudinary that have publicId
                for (ProductImageEntity oldImg : product.getProductImages()) {
                    if (oldImg.getPublicId() != null && !oldImg.getPublicId().isEmpty()) {
                        try {
                            cloudinaryService.deleteFile(oldImg.getPublicId());
                        } catch (Exception e) {
                            log.warn("Failed to delete old image from Cloudinary: {}", oldImg.getPublicId());
                        }
                    }
                }

                product.getProductImages().clear();
                List<ProductImageEntity> imageEntities = request.getImages().stream()
                        .map(imgReq -> ProductImageEntity.builder()
                                .imageUrl(imgReq.getImageUrl())
                                .imageType(imgReq.getImageType())
                                .publicId(imgReq.getPublicId())
                                .isPrimary(imgReq.isPrimary())
                                .product(product)
                                .build())
                        .toList();
                product.getProductImages().addAll(imageEntities);
            }

            // Handle multipart file uploads (append or replace)
            if (images != null && images.length > 0) {
                // If JSON images were also provided, they're already set above
                // If no JSON images, clear old images and upload new ones
                if (request.getImages() == null) {
                    // Delete old images from Cloudinary
                    for (ProductImageEntity oldImg : new ArrayList<>(product.getProductImages())) {
                        if (oldImg.getPublicId() != null && !oldImg.getPublicId().isEmpty()) {
                            try {
                                cloudinaryService.deleteFile(oldImg.getPublicId());
                            } catch (Exception e) {
                                log.warn("Failed to delete old image from Cloudinary: {}", oldImg.getPublicId());
                            }
                        }
                    }
                    product.getProductImages().clear();
                }
                uploadAndAttachImages(product, images);
            }

            // Ensure at least one image is marked as primary
            ensureOnePrimaryImage(product);

            // Sync legacy image field
            syncPrimaryImageField(product);

            productRepository.save(product);
            log.info("Product updated with {} images, id={}", product.getProductImages().size(), product.getId());
            return product.getId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
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

    // ========== Helper methods ==========

    /**
     * Upload multiple files to Cloudinary and attach as ProductImageEntity.
     * First image is marked as PRIMARY if no existing primary exists.
     */
    private void uploadAndAttachImages(ProductEntity product, MultipartFile[] files) throws IOException {
        boolean hasPrimary = product.getProductImages().stream().anyMatch(ProductImageEntity::isPrimary);

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (file == null || file.isEmpty()) continue;

            Map<?, ?> uploadResult = cloudinaryService.uploadFileWithInfo(file, "products");
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // First uploaded image becomes PRIMARY if no primary exists yet
            boolean isPrimary = !hasPrimary && i == 0;

            ProductImageEntity imageEntity = ProductImageEntity.builder()
                    .imageUrl(secureUrl)
                    .imageType(isPrimary ? "PRIMARY" : "COVER")
                    .publicId(publicId)
                    .isPrimary(isPrimary)
                    .product(product)
                    .build();

            product.getProductImages().add(imageEntity);

            if (isPrimary) {
                hasPrimary = true;
            }

            log.info("Uploaded image: url={}, type={}, isPrimary={}", secureUrl, imageEntity.getImageType(), isPrimary);
        }
    }

    /**
     * Ensure exactly one image is marked as primary.
     * If none is primary, set the first one as primary.
     */
    private void ensureOnePrimaryImage(ProductEntity product) {
        if (product.getProductImages().isEmpty()) return;

        boolean hasPrimary = product.getProductImages().stream().anyMatch(ProductImageEntity::isPrimary);
        if (!hasPrimary) {
            product.getProductImages().get(0).setPrimary(true);
            product.getProductImages().get(0).setImageType("PRIMARY");
        }
    }

    /**
     * Sync the legacy `image` field on ProductEntity with the primary image URL.
     * This provides backward compatibility for code that reads product.getImage().
     */
    private void syncPrimaryImageField(ProductEntity product) {
        product.getProductImages().stream()
                .filter(ProductImageEntity::isPrimary)
                .findFirst()
                .ifPresent(primaryImg -> product.setImage(primaryImg.getImageUrl()));
    }
}
