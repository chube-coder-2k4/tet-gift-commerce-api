package com.tetgift.mapper;

import com.tetgift.dto.response.ProductImageResponse;
import com.tetgift.dto.response.ProductResponse;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.model.entity.ProductImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "productImages", target = "images")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "productImages", target = "primaryImage", qualifiedByName = "extractPrimaryImageUrl")
    ProductResponse toResponse(ProductEntity product);

    List<ProductResponse> toResponseList(List<ProductEntity> products);

    @Mapping(source = "primary", target = "isPrimary")
    ProductImageResponse toImageResponse(ProductImageEntity image);

    /**
     * Extract the primary image URL from the list of product images.
     * Falls back to the first image URL, then to the product's legacy image field.
     */
    @Named("extractPrimaryImageUrl")
    default String extractPrimaryImageUrl(List<ProductImageEntity> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(ProductImageEntity::isPrimary)
                .findFirst()
                .map(ProductImageEntity::getImageUrl)
                .orElse(images.get(0).getImageUrl());
    }
}
