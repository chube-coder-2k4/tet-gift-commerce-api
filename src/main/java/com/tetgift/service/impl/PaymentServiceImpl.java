package com.tetgift.service.impl;

import com.tetgift.configuration.VNPayConfig;
import com.tetgift.dto.request.PaymentRequest;
import com.tetgift.dto.response.PaymentResponse;
import com.tetgift.enums.OrderStatus;
import com.tetgift.enums.PaymentMethod;
import com.tetgift.enums.PaymentStatus;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.entity.OrderEntity;
import com.tetgift.model.entity.PaymentEntity;
import com.tetgift.repository.jpa.OrderRepository;
import com.tetgift.repository.jpa.PaymentRepository;
import com.tetgift.service.PaymentService;
import com.tetgift.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VNPayConfig vnPayConfig;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidDataException("Order is not in CREATED status");
        }

        PaymentMethod method = PaymentMethod.valueOf(request.getMethod().toUpperCase());

        PaymentEntity payment = PaymentEntity.builder()
                .order(order)
                .method(method)
                .status(PaymentStatus.PENDING)
                .amount(order.getTotalAmount())
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        // Update order status
        order.setStatus(OrderStatus.WAITING_PAYMENT);
        orderRepository.save(order);

        String paymentUrl = null;
        if (method == PaymentMethod.VN_PAY) {
            paymentUrl = VNPayUtil.buildPaymentUrl(saved.getId(), saved.getAmount().doubleValue(), vnPayConfig);
        }

        return toResponse(saved, paymentUrl);
    }

    @Override
    @Transactional
    public PaymentResponse handleVnPayCallback(String transactionId, String responseCode) {
        Long paymentId;
        try {
            paymentId = Long.parseLong(transactionId);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid transaction ID");
        }

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionId);
            payment.setPaidAt(LocalDateTime.now());

            // Update order status
            OrderEntity order = payment.getOrder();
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        PaymentEntity updated = paymentRepository.save(payment);
        log.info("VNPay callback: payment {} - status {}", paymentId, updated.getStatus());
        return toResponse(updated, null);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        return toResponse(payment, null);
    }

    private PaymentResponse toResponse(PaymentEntity payment, String paymentUrl) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .method(payment.getMethod().name())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .paymentUrl(paymentUrl)
                .build();
    }
}
