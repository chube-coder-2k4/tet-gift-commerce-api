package com.tetgift.mapper;

import com.tetgift.dto.request.BundleProductRequest;
import com.tetgift.dto.request.BundleRequest;
import com.tetgift.model.entity.BundleEntity;
import com.tetgift.model.entity.BundleProductEntity;
import com.tetgift.model.entity.ProductEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface BundleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "bundleProducts", source = "products")
    BundleEntity toEntity(BundleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "bundle", ignore = true)
    @Mapping(target = "product", expression = "java(productFromId(request.getProductId()))")
    @Mapping(source = "quantity", target = "quantity", defaultValue = "1")
    BundleProductEntity toBundleProductEntity(BundleProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "bundleProducts", ignore = true)
    void updateEntity(@MappingTarget BundleEntity entity, BundleRequest request);

    default ProductEntity productFromId(Long productId) {
        if (productId == null) return null;
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        return product;
    }
}
