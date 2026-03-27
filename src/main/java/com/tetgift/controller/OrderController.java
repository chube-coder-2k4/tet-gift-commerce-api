package com.tetgift.controller;

import com.tetgift.dto.request.OrderRequest;
import com.tetgift.dto.request.RefundRequest;
import com.tetgift.dto.response.OrderResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order from cart", description = "Create a new order from the current user's cart")
    public ResponseEntity<ResponseData<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>(HttpStatus.CREATED.value(), "Order created successfully",
                        orderService.createOrder(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ResponseData<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Order fetched",
                        orderService.getOrderById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all orders or my orders depending on default behavior or redirect")
    public ResponseEntity<ResponseData<PageResponse<OrderResponse>>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "All orders fetched",
                        orderService.getAllOrders(page, size)));
    }
        @GetMapping("/track/{orderCode}")
        @Operation(summary = "Track order by Code", description = "Publicly track an order using order code")
        public ResponseEntity<ResponseData<OrderResponse>> trackOrderByCode(@PathVariable String orderCode) {
                return ResponseEntity
                                .ok(new ResponseData<>(HttpStatus.OK.value(), "Order fetched via tracking",
                                                orderService.getOrderByCode(orderCode)));
        }

        @GetMapping("/my-orders")
        @Operation(summary = "Get my orders", description = "Get paginated list of current user's orders")
        public ResponseEntity<ResponseData<PageResponse<OrderResponse>>> getMyOrders(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return ResponseEntity
                                .ok(new ResponseData<>(HttpStatus.OK.value(), "Orders fetched",
                                                orderService.getMyOrders(page, size)));
        }

    @GetMapping("/all")
    @Operation(summary = "Get all orders (ADMIN)", description = "Get paginated list of all orders")
    public ResponseEntity<ResponseData<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "All orders fetched",
                        orderService.getAllOrders(page, size)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status (ADMIN)", description = "Update the status of an order")
    public ResponseEntity<ResponseData<OrderResponse>> updateStatus(@PathVariable Long id,
                                                                    @RequestParam String status) {
        return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Order status updated",
                orderService.updateOrderStatus(id, status)));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel order (USER)", description = "Cancel an order (only if CREATED or WAITING_PAYMENT)")
    public ResponseEntity<ResponseData<OrderResponse>> cancelOrder(@PathVariable Long id) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Order cancelled",
                        orderService.cancelOrder(id)));
    }

    @PutMapping("/{id}/cancel-refund")
    @Operation(summary = "Cancel paid order with refund (USER)",
            description = "Cancel a paid order and request refund with bank info")
    public ResponseEntity<ResponseData<OrderResponse>> cancelOrderWithRefund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(),
                        "Order cancelled. Refund is pending.",
                        orderService.cancelOrderWithRefund(id, request)));
    }
}

