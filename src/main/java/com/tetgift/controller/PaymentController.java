package com.tetgift.controller;

import com.tetgift.dto.request.PaymentRequest;
import com.tetgift.dto.response.PaymentResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.dto.response.VnPayIpnResponse;
import com.tetgift.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.TreeMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Management", description = "APIs for managing payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create payment", description = "Create a payment for an order (COD or VNPay)")
    public ResponseEntity<ResponseData<PaymentResponse>> create(@RequestBody @Valid PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>(HttpStatus.CREATED.value(), "Payment created",
                        paymentService.createPayment(request)));
    }

    @GetMapping("/vnpay-callback")
    @Operation(summary = "VNPay callback", description = "Handle VNPay payment callback")
    public ResponseEntity<ResponseData<PaymentResponse>> vnpayCallback(
            @RequestParam Map<String, String> requestParams) {
        // Convert request params to a TreeMap to sort keys
        Map<String, String> vnpParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                vnpParams.put(entry.getKey(), entry.getValue());
            }
        }
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Payment callback processed",
                paymentService.handleVnPayCallback(vnpParams)));
    }

    @GetMapping("/vnpay-ipn")
    @Operation(summary = "VNPay IPN", description = "Handle VNPay IPN callback (Server to Server)")
    public ResponseEntity<VnPayIpnResponse> vnpayIpn(@RequestParam Map<String, String> requestParams) {
        // Convert request params to a TreeMap to sort keys
        Map<String, String> vnpParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                vnpParams.put(entry.getKey(), entry.getValue());
            }
        }
        return ResponseEntity.ok(paymentService.handleVnPayIpn(vnpParams));
    }

    @GetMapping("/{orderId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<ResponseData<PaymentResponse>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Payment fetched",
                paymentService.getPaymentByOrderId(orderId)));
    }
}
