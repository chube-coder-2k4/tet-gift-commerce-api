package com.tetgift.repository.jpa;

import com.tetgift.model.entity.CartEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"cartItems", "cartItems.product", "cartItems.bundle"})
    Optional<CartEntity> findWithItemsByUserId(Long userId);
}
