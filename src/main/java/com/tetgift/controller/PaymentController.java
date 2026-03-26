package com.tetgift.controller;

import com.tetgift.configuration.VNPayConfig;
import com.tetgift.dto.request.PaymentRequest;
import com.tetgift.dto.response.PaymentResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.dto.response.VnPayIpnResponse;
import com.tetgift.service.InvoiceService;
import com.tetgift.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Management", description = "APIs for managing payments")
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final VNPayConfig vnPayConfig;

    @PostMapping("/create")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create payment", description = "Create a payment for an order (COD or VNPay)")
    public ResponseEntity<ResponseData<PaymentResponse>> create(@RequestBody @Valid PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>(HttpStatus.CREATED.value(), "Payment created",
                        paymentService.createPayment(request)));
    }

    @PostMapping("/{orderId}/retry-vnpay")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retry VNPay Payment", description = "Retry failed or uncompleted VNPay payment for an order")
    public ResponseEntity<ResponseData<PaymentResponse>> retryVnPayPayment(@PathVariable Long orderId) {
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(), "New payment URL created",
                paymentService.retryVnPayPayment(orderId)
        ));
    }

    /**
     * VNPay redirects user browser here after payment.
     * BE verifies signature, updates order/payment status, then 302 redirects to FE payment-result page.
     * Invoice generation happens AFTER transaction commits to avoid rollback issues.
     */
    @GetMapping("/vnpay-callback")
    @Operation(summary = "VNPay callback", description = "Handle VNPay payment callback - redirects to frontend")
    public ResponseEntity<Void> vnpayCallback(@RequestParam Map<String, String> requestParams) {
        log.info("VNPay callback received with params: {}", requestParams.keySet());

        Map<String, String> vnpParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                vnpParams.put(entry.getKey(), entry.getValue());
            }
        }

        String status;
        String orderId = "";
        String amount = "";

        try {
            // Step 1: Process payment (transactional - commits here)
            PaymentResponse response = paymentService.handleVnPayCallback(vnpParams);
            status = response.getStatus(); // "SUCCESS" or "FAILED"
            orderId = String.valueOf(response.getOrderId());
            amount = response.getAmount() != null ? response.getAmount().toPlainString() : "0";
            log.info("VNPay callback processed: orderId={}, status={}", orderId, status);

            // Step 2: Generate invoice AFTER transaction committed (non-blocking)
            if ("SUCCESS".equals(status) && response.getOrderId() != null) {
                try {
                    invoiceService.generateInvoice(response.getOrderId());
                    log.info("Invoice auto-generated for order: {}", response.getOrderId());
                } catch (Exception invoiceEx) {
                    log.warn("Failed to auto-generate invoice for order {}: {}",
                            response.getOrderId(), invoiceEx.getMessage());
                    // Don't affect payment result - invoice can be generated later
                }
            }
        } catch (Exception e) {
            log.error("VNPay callback failed: {}", e.getMessage());
            status = "FAILED";
        }

        // Build FE redirect URL
        String frontendUrl = vnPayConfig.getFrontendResultUrl();
        String redirectUrl = frontendUrl
                + "?status=" + status
                + "&orderId=" + orderId
                + "&amount=" + amount;

        log.info("Redirecting to FE: {}", redirectUrl);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @GetMapping("/vnpay-ipn")
    @Operation(summary = "VNPay IPN", description = "Handle VNPay IPN callback (Server to Server)")
    public ResponseEntity<VnPayIpnResponse> vnpayIpn(@RequestParam Map<String, String> requestParams) {
        log.info("VNPay IPN received with params: {}", requestParams.keySet());

        Map<String, String> vnpParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                vnpParams.put(entry.getKey(), entry.getValue());
            }
        }
        VnPayIpnResponse response = paymentService.handleVnPayIpn(vnpParams);
        log.info("VNPay IPN result: code={}, msg={}", response.getRspCode(), response.getMessage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<ResponseData<PaymentResponse>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Payment fetched",
                paymentService.getPaymentByOrderId(orderId)));
    }
}
