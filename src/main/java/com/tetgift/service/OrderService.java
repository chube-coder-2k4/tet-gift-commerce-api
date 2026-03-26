package com.tetgift.service;

import com.tetgift.dto.request.OrderRequest;
import com.tetgift.dto.request.RefundRequest;
import com.tetgift.dto.response.OrderResponse;
import com.tetgift.dto.response.PageResponse;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    PageResponse<OrderResponse> getMyOrders(int page, int size);

    PageResponse<OrderResponse> getAllOrders(int page, int size);

    OrderResponse updateOrderStatus(Long id, String status);

    OrderResponse cancelOrder(Long id);

    OrderResponse cancelOrderWithRefund(Long orderId, RefundRequest request);

    PageResponse<OrderResponse> getRefundOrders(int page, int size);

    OrderResponse confirmRefund(Long orderId);
}

