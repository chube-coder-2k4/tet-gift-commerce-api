package com.tetgift.dto.response;




import lombok.Data;
import java.math.BigDecimal;
import java.util.List;


@Data
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String status;
    private List<ProductImageResponse> images;
    private List<ProductBadgeResponse> badges;
    private Double ratingAvg;
    private Integer ratingCount;
}
