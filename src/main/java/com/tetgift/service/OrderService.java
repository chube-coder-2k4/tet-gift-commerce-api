package com.tetgift.service;

import com.tetgift.dto.request.OrderRequest;
import com.tetgift.dto.response.OrderResponse;
import com.tetgift.dto.response.PageResponse;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByCode(String orderCode);

    PageResponse<OrderResponse> getMyOrders(int page, int size);

    PageResponse<OrderResponse> getAllOrders(int page, int size);

    OrderResponse updateOrderStatus(Long id, String status);

    OrderResponse cancelOrder(Long id);
}
