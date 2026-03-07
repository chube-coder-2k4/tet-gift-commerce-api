package com.tetgift.mapper;

import com.tetgift.dto.response.ProductImageResponse;
import com.tetgift.dto.response.ProductResponse;
import com.tetgift.model.entity.ProductEntity;
import com.tetgift.model.entity.ProductImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "productImages", target = "images")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    ProductResponse toResponse(ProductEntity product);

    List<ProductResponse> toResponseList(List<ProductEntity> products);

    @Mapping(source = "primary", target = "isPrimary")
    ProductImageResponse toImageResponse(ProductImageEntity image);
}
