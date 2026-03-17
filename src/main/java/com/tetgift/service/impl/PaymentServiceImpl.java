package com.tetgift.service.impl;

import com.tetgift.configuration.VNPayConfig;
import com.tetgift.dto.request.PaymentRequest;
import com.tetgift.dto.response.PaymentResponse;
import com.tetgift.dto.response.VnPayIpnResponse;
import com.tetgift.enums.OrderStatus;
import com.tetgift.enums.PaymentMethod;
import com.tetgift.enums.PaymentStatus;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.entity.OrderEntity;
import com.tetgift.model.entity.PaymentEntity;
import com.tetgift.repository.jpa.CartRepository;
import com.tetgift.repository.jpa.OrderRepository;
import com.tetgift.repository.jpa.PaymentRepository;
import com.tetgift.service.PaymentService;
import com.tetgift.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final VNPayConfig vnPayConfig;

    private static final long MIN_PAYMENT_AMOUNT = 5000; // 5,000 VNĐ

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidDataException("Order is not in CREATED status");
        }

        PaymentMethod method = PaymentMethod.valueOf(request.getMethod().toUpperCase());

        // === Payment amount validation ===
        long totalAmountLong = order.getTotalAmount().longValue();

        // Case 1: Total = 0 (free order from discount) → auto-success
        if (totalAmountLong == 0) {
            PaymentEntity payment = PaymentEntity.builder()
                    .order(order)
                    .method(method)
                    .status(PaymentStatus.SUCCESS)
                    .amount(order.getTotalAmount())
                    .paidAt(LocalDateTime.now())
                    .transactionId("FREE_ORDER_" + order.getId())
                    .build();

            PaymentEntity saved = paymentRepository.save(payment);

            // Auto-complete order
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            // Clear cart immediately
            clearCartForUser(order.getUser().getId());

            log.info("Free order auto-completed: orderId={}, userId={}", order.getId(), order.getUser().getId());
            return toResponse(saved, null);
        }

        // Case 2: 0 < total < 5000 → reject
        if (totalAmountLong > 0 && totalAmountLong < MIN_PAYMENT_AMOUNT) {
            throw new InvalidDataException(
                    "Tổng đơn hàng phải ít nhất " + MIN_PAYMENT_AMOUNT + " VNĐ. Đơn hàng hiện tại: "
                            + order.getTotalAmount() + " VNĐ");
        }

        // Case 3: total >= 5000 → normal payment flow
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
        } else if (method == PaymentMethod.COD) {
            // COD: clear cart immediately since no online payment needed
            clearCartForUser(order.getUser().getId());
        }

        return toResponse(saved, paymentUrl);
    }

    @Override
    @Transactional
    public PaymentResponse handleVnPayCallback(Map<String, String> requestParams) {
        // Verify signature
        String vnp_SecureHash = requestParams.get("vnp_SecureHash");
        if (requestParams.containsKey("vnp_SecureHashType")) {
            requestParams.remove("vnp_SecureHashType");
        }
        if (requestParams.containsKey("vnp_SecureHash")) {
            requestParams.remove("vnp_SecureHash");
        }

        String signValue = VNPayUtil.hashAllFields(requestParams);
        String vnp_SecureHash_Check = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), signValue);

        if (!vnp_SecureHash_Check.equals(vnp_SecureHash)) {
            throw new InvalidDataException("Invalid Signature");
        }

        String transactionId = requestParams.get("vnp_TxnRef");
        String responseCode = requestParams.get("vnp_ResponseCode");

        Long paymentId;
        try {
            paymentId = Long.parseLong(transactionId);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid transaction ID");
        }

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if ("00".equals(responseCode)) {
            // Only update if not already success
            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setTransactionId(transactionId);
                payment.setPaidAt(LocalDateTime.now());

                OrderEntity order = payment.getOrder();
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                // VNPay success: clear cart now
                clearCartForUser(order.getUser().getId());
            }
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

    @Override
    @Transactional
    public VnPayIpnResponse handleVnPayIpn(Map<String, String> requestParams) {
        // Build checksum hash
        String vnp_SecureHash = requestParams.get("vnp_SecureHash");
        if (requestParams.containsKey("vnp_SecureHashType")) {
            requestParams.remove("vnp_SecureHashType");
        }
        if (requestParams.containsKey("vnp_SecureHash")) {
            requestParams.remove("vnp_SecureHash");
        }

        // Check checksum
        String signValue = VNPayUtil.hashAllFields(requestParams);
        String vnp_SecureHash_Check = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), signValue);

        if (!vnp_SecureHash_Check.equals(vnp_SecureHash)) {
            return VnPayIpnResponse.builder().rspCode("97").message("Invalid Checksum").build();
        }

        // Handle payment logic
        String txnRef = requestParams.get("vnp_TxnRef");
        String responseCode = requestParams.get("vnp_ResponseCode");
        String transactionNo = requestParams.get("vnp_TransactionNo");
        String amount = requestParams.get("vnp_Amount");

        long paymentId;
        try {
            paymentId = Long.parseLong(txnRef);
        } catch (NumberFormatException e) {
            return VnPayIpnResponse.builder().rspCode("01").message("Order not found").build();
        }

        PaymentEntity payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            return VnPayIpnResponse.builder().rspCode("01").message("Order not found").build();
        }

        // Check amount if necessary (compare request amount vs saved amount)
        long vnpAmount = Long.parseLong(amount);
        long checkAmount = (long) (payment.getAmount().doubleValue() * 100);

        if (vnpAmount != checkAmount) {
            return VnPayIpnResponse.builder().rspCode("04").message("Invalid Amount").build();
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return VnPayIpnResponse.builder().rspCode("02").message("Order already confirmed").build();
        }

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionNo);
            payment.setPaidAt(LocalDateTime.now());

            OrderEntity order = payment.getOrder();
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            // VNPay IPN success: clear cart now
            clearCartForUser(order.getUser().getId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);

        return VnPayIpnResponse.builder().rspCode("00").message("Confirm Success").build();
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

    private void clearCartForUser(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getCartItems().clear();
            cartRepository.save(cart);
            log.info("Cart cleared for user: {}", userId);
        });
    }

}
