package com.tetgift.repository.jpa;

import com.tetgift.model.entity.InventoryBatchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatchEntity, Long> {

    Page<InventoryBatchEntity> findByProductId(Long productId, Pageable pageable);

    // Tìm các lô còn hàng và chưa hết hạn, sắp xếp theo hạn dùng gần nhất (FEFO)
    List<InventoryBatchEntity> findByProductIdAndCurrentQuantityGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(
            Long productId, Integer quantity, LocalDate now);

    // Tìm tất cả lô của một sản phẩm (để xem lịch sử trong Admin)
    List<InventoryBatchEntity> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Lấy tất cả lô hàng, sắp xếp theo ngày hết hạn (lô nào sắp hết hạn hiện lên trước)
    @Query("SELECT b FROM InventoryBatchEntity b JOIN FETCH b.product")
    Page<InventoryBatchEntity> findAllWithProduct(Pageable pageable);

    // Lấy danh sách lô theo từng sản phẩm cụ thể
    List<InventoryBatchEntity> findByProductIdOrderByExpiryDateAsc(Long productId);

    @Query("SELECT b FROM InventoryBatchEntity b JOIN FETCH b.product p " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.batchCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<InventoryBatchEntity> searchBatches(@Param("keyword") String keyword, Pageable pageable);
}