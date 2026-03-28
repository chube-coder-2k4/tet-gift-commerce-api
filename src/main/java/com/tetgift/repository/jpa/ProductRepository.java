package com.tetgift.repository.jpa;

import com.tetgift.model.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findByIsActiveTrue(Pageable pageable);

    Optional<ProductEntity> findByName(String name);

    Optional<ProductEntity> findByIdAndIsActiveTrue(Long id);

    Page<ProductEntity> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);



    /**
     * Find products by max price
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.isActive = true AND p.price <= :maxPrice ORDER BY p.price ASC")
    List<ProductEntity> findByPriceLessThanEqual(@Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find products by price range
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.isActive = true AND p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    List<ProductEntity> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find products by category name (case-insensitive)
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.isActive = true AND LOWER(p.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))")
    List<ProductEntity> findByCategoryNameContaining(@Param("categoryName") String categoryName);

    /**
     * Find products with stock greater than specified amount
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.isActive = true AND p.stock >= :minStock ORDER BY p.stock DESC")
    List<ProductEntity> findByStockGreaterThanEqual(@Param("minStock") Integer minStock);

    /**
     * Search products by keyword in name or description
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ProductEntity> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find products by max price and minimum stock
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.isActive = true AND p.price <= :maxPrice AND p.stock >= :minStock ORDER BY p.price ASC")
    List<ProductEntity> findByPriceAndAvailability(@Param("maxPrice") BigDecimal maxPrice, @Param("minStock") Integer minStock);

    /**
     * Find top products for general recommendations
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.isActive = true AND p.stock > 0 ORDER BY p.createdAt DESC")
    List<ProductEntity> findTopActiveProducts(Pageable pageable);


    /**
     * Tính tổng tồn kho khả dụng từ các lô hàng (chưa hết hạn và còn hàng)
     */
    @Query("SELECT COALESCE(SUM(b.currentQuantity), 0) FROM InventoryBatchEntity b " +
            "WHERE b.product.id = :productId AND b.isActive = true AND b.expiryDate >= CURRENT_DATE")
    Integer getTotalEffectiveStock(@Param("productId") Long productId);

    /**
     * Tìm các sản phẩm có tổng tồn kho từ các lô hàng >= minStock
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.isActive = true AND " +
            "(SELECT COALESCE(SUM(b.currentQuantity), 0) FROM InventoryBatchEntity b " +
            "WHERE b.product = p AND b.isActive = true AND b.expiryDate >= CURRENT_DATE) >= :minStock")
    List<ProductEntity> findByBatchStockGreaterThanEqual(@Param("minStock") Integer minStock);
}
