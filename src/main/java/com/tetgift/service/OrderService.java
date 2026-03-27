package com.tetgift.service;

import com.tetgift.dto.request.OrderRequest;
import com.tetgift.dto.request.RefundRequest;
import com.tetgift.dto.response.OrderResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.model.entity.ProductEntity;

import java.time.LocalDateTime;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByCode(String orderCode);

    PageResponse<OrderResponse> getMyOrders(int page, int size);

    PageResponse<OrderResponse> getAllOrders(int page, int size);

    OrderResponse updateOrderStatus(Long id, String status);

    OrderResponse cancelOrder(Long id);

    void deductStockFromBatches(ProductEntity product, int neededQuantity);
    OrderResponse cancelOrderWithRefund(Long orderId, RefundRequest request);

    PageResponse<OrderResponse> getRefundOrders(String filterStatus, int page, int size);

    OrderResponse confirmRefund(Long orderId);

    byte[] exportRefundOrders(String filterStatus, LocalDateTime startDate, LocalDateTime endDate, String format);

}

