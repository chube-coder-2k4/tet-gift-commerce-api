package com.tetgift.repository.jpa;

import com.tetgift.model.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Page<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query(value = "SELECT new com.tetgift.dto.response.TopCustomerResponse(u.id, u.fullName, u.email, COUNT(o), SUM(o.totalAmount)) " +
           "FROM OrderEntity o JOIN o.user u " +
           "WHERE o.status NOT IN ('CANCELLED', 'CREATED', 'WAITING_PAYMENT') " +
           "GROUP BY u.id, u.fullName, u.email " +
           "ORDER BY SUM(o.totalAmount) DESC",
           countQuery = "SELECT COUNT(DISTINCT o.user.id) " +
           "FROM OrderEntity o " +
           "WHERE o.status NOT IN ('CANCELLED', 'CREATED', 'WAITING_PAYMENT')")
    Page<com.tetgift.dto.response.TopCustomerResponse> findTopCustomers(Pageable pageable);
}
