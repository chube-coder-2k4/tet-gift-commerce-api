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
import org.springframework.http.HttpStatus;
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
    @Operation(summary = "Get refund requests", description = "Get paginated list of orders pending refund")
    public ResponseEntity<ResponseData<PageResponse<OrderResponse>>> getRefundOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(new ResponseData<>(HttpStatus.OK.value(), "Refund orders fetched",
                        orderService.getRefundOrders(page, size)));
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
    public void exportRefunds(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "xlsx") String format,
            HttpServletResponse response) throws IOException {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<OrderEntity> orders = orderRepository.findByStatusAndCreatedAtBetween(
                OrderStatus.CANCELLED_PENDING_REFUND, startDateTime, endDateTime);

        if ("csv".equalsIgnoreCase(format)) {
            exportCsv(orders, response);
        } else {
            exportExcel(orders, response);
        }
    }

    private void exportExcel(List<OrderEntity> orders, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=refund_orders.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Refund Orders");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Header row
            String[] headers = {
                    "Order ID", "Customer Name", "Customer Email",
                    "Total Amount", "Order Date",
                    "Bank Name", "Bank Account", "Account Holder"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (OrderEntity order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getUser().getFullName());
                row.createCell(2).setCellValue(order.getUser().getEmail());
                row.createCell(3).setCellValue(order.getTotalAmount().doubleValue());
                row.createCell(4).setCellValue(order.getCreatedAt().toString());
                row.createCell(5).setCellValue(order.getRefundBankName() != null ? order.getRefundBankName() : "");
                row.createCell(6).setCellValue(order.getRefundBankAccount() != null ? order.getRefundBankAccount() : "");
                row.createCell(7).setCellValue(order.getRefundAccountHolder() != null ? order.getRefundAccountHolder() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    private void exportCsv(List<OrderEntity> orders, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=refund_orders.csv");
        response.setCharacterEncoding("UTF-8");

        // Add BOM for UTF-8 Excel compatibility
        response.getOutputStream().write(0xEF);
        response.getOutputStream().write(0xBB);
        response.getOutputStream().write(0xBF);

        PrintWriter writer = response.getWriter();
        writer.println("Order ID,Customer Name,Customer Email,Total Amount,Order Date,Bank Name,Bank Account,Account Holder");

        for (OrderEntity order : orders) {
            writer.printf("%d,\"%s\",\"%s\",%s,%s,\"%s\",\"%s\",\"%s\"%n",
                    order.getId(),
                    escapeCsv(order.getUser().getFullName()),
                    escapeCsv(order.getUser().getEmail()),
                    order.getTotalAmount().toPlainString(),
                    order.getCreatedAt().toString(),
                    escapeCsv(order.getRefundBankName()),
                    escapeCsv(order.getRefundBankAccount()),
                    escapeCsv(order.getRefundAccountHolder()));
        }

        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
