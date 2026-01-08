package com.tetgift.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusMessage {
    private String orderId;
    private String status;
    private String message;
}