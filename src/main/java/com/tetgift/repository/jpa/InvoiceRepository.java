package com.tetgift.repository.jpa;

import com.tetgift.model.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    Optional<InvoiceEntity> findByOrderId(Long orderId);
    Optional<InvoiceEntity> findByInvoiceNumber(String invoiceNumber);
    boolean existsByOrderId(Long orderId);
}
