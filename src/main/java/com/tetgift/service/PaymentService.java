package com.tetgift.service;

import com.tetgift.dto.request.PaymentRequest;
import com.tetgift.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse handleVnPayCallback(String transactionId, String responseCode);

    PaymentResponse getPaymentByOrderId(Long orderId);
}
