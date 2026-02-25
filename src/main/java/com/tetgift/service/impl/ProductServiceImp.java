package com.tetgift.service.impl;

import com.tetgift.dto.request.ProductRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ProductResponse;
import com.tetgift.exception.BadgeNotFoundException;
import com.tetgift.exception.ProductNotFoundException;
import com.tetgift.mapper.ProductMapper;
import com.tetgift.model.entity.*;
import com.tetgift.repository.jpa.ProductBadgeRepository;
import com.tetgift.repository.jpa.ProductRepository;
import com.tetgift.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImp implements ProductService {

    private final ProductRepository productRepository;
    private final ProductBadgeRepository productBadgeRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse findProductById(Long id) {
        ProductEntity productEntity = productRepository.findByStatusAndId(ProductStatus.ACTIVE,id)
                .orElseThrow(() -> new ProductNotFoundException("Product with " + id + " not found"));
        return productMapper.toResponse(productEntity);
    }

    @Override
    @Transactional
    public Long saveProduct(ProductRequest productRequest) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(productRequest.getName());
        productEntity.setDescription(productRequest.getDescription());
        productEntity.setPrice(productRequest.getPrice());
        productEntity.setOriginalPrice(productRequest.getOriginalPrice());
        productEntity.setStatus(ProductStatus.ACTIVE);

        if (productRequest.getInventory() != null) {
            ProductInventoryEntity inventoryEntity = new ProductInventoryEntity();
            inventoryEntity.setStockQuantity(productRequest.getInventory().getStockQuantity());
            inventoryEntity.setInStock(productRequest.getInventory().getStockQuantity() > 0);
            inventoryEntity.setProduct(productEntity);
            productEntity.setProductInventory(inventoryEntity);
        }

        if (productRequest.getImages() != null && !productRequest.getImages().isEmpty()) {
            List<ProductImageEntity> images = productRequest.getImages().stream()
                    .map(imgReq -> {
                        ProductImageEntity image = new ProductImageEntity();
                        image.setImageUrl(imgReq.getImageUrl());
                        image.setIsThumbnail(imgReq.getIsThumbnail());
                        image.setSortOrder(imgReq.getSortOrder());
                        image.setProduct(productEntity);
                        return image;
                    })
                    .toList();
            productEntity.setProductImages(images);
        }


            productEntity.setProductBadges(resolveBadges(productRequest.getBadgeIds()));

        ProductEntity savedProduct = productRepository.save(productEntity);
        return savedProduct.getId();
    }

    @Override
    public Long updateProduct(Long id, ProductRequest productRequest) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with " + id + " not found"));
        // Update fields as necessary

        productEntity.setName(productRequest.getName());
        productEntity.setDescription(productRequest.getDescription());
        productEntity.setPrice(productRequest.getPrice());
        productEntity.setOriginalPrice(productRequest.getOriginalPrice());

        if (productRequest.getInventory() != null) {
            if (productEntity.getProductInventory() == null) {
                ProductInventoryEntity inventoryEntity = new ProductInventoryEntity();
                inventoryEntity.setStockQuantity(productRequest.getInventory().getStockQuantity());
                inventoryEntity.setInStock(productRequest.getInventory().getStockQuantity() > 0);
                inventoryEntity.setProduct(productEntity);
                productEntity.setProductInventory(inventoryEntity);
            } else {
                productEntity.getProductInventory().setStockQuantity(productRequest.getInventory().getStockQuantity());
                productEntity.getProductInventory().setInStock(productRequest.getInventory().getStockQuantity() > 0);
            }
        }
        if (productRequest.getImages() != null && !productRequest.getImages().isEmpty()) {
            productEntity.getProductImages().clear();
            List<ProductImageEntity> images = productRequest.getImages().stream()
                    .map(imgReq -> {
                        ProductImageEntity image = new ProductImageEntity();
                        image.setImageUrl(imgReq.getImageUrl());
                        image.setIsThumbnail(imgReq.getIsThumbnail());
                        image.setSortOrder(imgReq.getSortOrder());
                        image.setProduct(productEntity);
                        return image;
                    })
                    .toList();
            productEntity.getProductImages().addAll(images);
        }


        productEntity.setProductBadges(resolveBadges(productRequest.getBadgeIds()));
        ProductEntity updatedProduct = productRepository.save(productEntity);

        return updatedProduct.getId();
    }

    @Override
    public void deleteProduct(Long id) {
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with " + id + " not found"));
        if(ProductStatus.DELETED.equals(productEntity.getStatus())){
            throw new ProductNotFoundException("Product with " + id + " Is already deleted");
        }
        productEntity.setStatus(ProductStatus.DELETED);
        productRepository.save(productEntity);
    }

    @Override
    public PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductEntity> products = productRepository.findByStatus(ProductStatus.ACTIVE,pageable);
        List<ProductResponse> productResponses = productMapper.toResponseList(products.getContent());

        return PageResponse.<ProductResponse>builder()
                .data(productResponses)
                .pageNo(page)
                .pageSize(size)
                .totalItems(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build();
    }

    public Set<ProductBadgeEntity> resolveBadges(Set<Long> badgeIds) {
        if (badgeIds == null || badgeIds.isEmpty()) {
            return Set.of();
        }

        // Remove duplicates early
        Set<Long> uniqueIds = new HashSet<>(badgeIds);

        List<ProductBadgeEntity> badges =
                productBadgeRepository.findAllById(uniqueIds);

        if (badges.size() != uniqueIds.size()) {

            Set<Long> foundIds = badges.stream()
                    .map(ProductBadgeEntity::getId)
                    .collect(Collectors.toSet());

            List<Long> missingIds = uniqueIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new BadgeNotFoundException("Badges not found for IDs: " + missingIds);
        }

        return new HashSet<>(badges);
    }

}
