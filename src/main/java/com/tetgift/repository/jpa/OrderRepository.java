package com.tetgift.repository.jpa;

import com.tetgift.enums.OrderStatus;
import com.tetgift.model.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Page<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);

    Page<OrderEntity> findByStatusIn(List<OrderStatus> statuses, Pageable pageable);

    List<OrderEntity> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);
    
    Optional<OrderEntity> findByOrderCode(String orderCode);

    List<OrderEntity> findByStatusInAndCreatedAtBetween(
            List<OrderStatus> statuses,
            LocalDateTime startDate,
            LocalDateTime endDate);

    @Query(value = "SELECT new com.tetgift.dto.response.TopCustomerResponse(u.id, u.fullName, u.email, COUNT(o), SUM(o.totalAmount)) " +
           "FROM OrderEntity o JOIN o.user u " +
           "WHERE o.status NOT IN ('CANCELLED', 'CREATED', 'WAITING_PAYMENT') " +
           "GROUP BY u.id, u.fullName, u.email " +
           "ORDER BY SUM(o.totalAmount) DESC",
           countQuery = "SELECT COUNT(DISTINCT o.user.id) " +
           "FROM OrderEntity o " +
           "WHERE o.status NOT IN ('CANCELLED', 'CREATED', 'WAITING_PAYMENT')")
    Page<com.tetgift.dto.response.TopCustomerResponse> findTopCustomers(Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.status IN :statuses " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(o.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<OrderEntity> findRefundOrders(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("keyword") String keyword,
            Pageable pageable);

}
