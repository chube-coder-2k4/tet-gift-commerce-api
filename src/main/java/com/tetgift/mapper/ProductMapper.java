package com.tetgift.mapper;


import com.tetgift.dto.response.ProductResponse;
import com.tetgift.model.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
                        //entity                //response
    @Mapping(source = "productImages", target = "images")
    @Mapping(source = "productBadges", target = "badges")
    @Mapping(source = "productRatingSummary.ratingAvg", target = "ratingAvg")
    @Mapping(source = "productRatingSummary.ratingCount", target = "ratingCount")
    ProductResponse toResponse(ProductEntity product);

    List<ProductResponse> toResponseList(List<ProductEntity> products);
}



