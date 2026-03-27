package com.tetgift.service;

import com.tetgift.dto.request.PaymentRequest;
import com.tetgift.dto.response.PaymentResponse;
import com.tetgift.dto.response.VnPayIpnResponse;
import java.util.Map;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse retryVnPayPayment(Long orderId);

    PaymentResponse handleVnPayCallback(Map<String, String> requestParams);

    VnPayIpnResponse handleVnPayIpn(Map<String, String> requestParams);

    PaymentResponse getPaymentByOrderId(Long orderId);
}
