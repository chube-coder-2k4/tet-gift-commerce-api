package com.tetgift.controller;

import com.tetgift.dto.response.OrderResponse;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.enums.OrderStatus;
import com.tetgift.model.entity.OrderEntity;
import com.tetgift.repository.jpa.OrderRepository;
import com.tetgift.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/refunds")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Refund Management (ADMIN)", description = "APIs for managing refund requests")
@Slf4j
public class RefundController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping
    @Operation(summary = "Get refund requests", description = "Get paginated list of orders pending/completed refund")
    public ResponseEntity<ResponseData<PageResponse<OrderResponse>>> getRefundOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Refund orders fetched",
                        orderService.getRefundOrders(keyword, status, page, size)));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm refund", description = "Mark order refund as completed")
    public ResponseEntity<ResponseData<OrderResponse>> confirmRefund(@PathVariable Long id) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Refund confirmed",
                        orderService.confirmRefund(id)));
    }

    @GetMapping("/export")
    @Operation(summary = "Export refund list",
            description = "Export refund orders to Excel or CSV. Format: xlsx or csv")
    public ResponseEntity<byte[]> exportRefunds(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "xlsx") String format) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        byte[] fileContent = orderService.exportRefundOrders(startDateTime, endDateTime, status, format);

        HttpHeaders headers = new HttpHeaders();
        if ("csv".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "refund_orders.csv");
        } else {
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "refund_orders.xlsx");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }
}
