package com.tetgift.controller;

import com.tetgift.dto.response.InvoiceResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice", description = "Invoice management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Generate invoice for an order (auto-called on payment success, but can also be called manually)
     */
    @PostMapping("/generate/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Generate invoice", description = "Generate PDF invoice for a paid order")
    public ResponseEntity<ResponseData<InvoiceResponse>> generateInvoice(@PathVariable Long orderId) {
        log.info("Generating invoice for order: {}", orderId);
        InvoiceResponse response = invoiceService.generateInvoice(orderId);

        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Invoice generated successfully",
                response
        ));
    }

    /**
     * Get invoice info by order ID
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get invoice by order", description = "Get invoice details for a specific order")
    public ResponseEntity<ResponseData<InvoiceResponse>> getInvoiceByOrderId(@PathVariable Long orderId) {
        InvoiceResponse response = invoiceService.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Invoice fetched successfully",
                response
        ));
    }

    /**
     * Download invoice PDF directly
     */
    @GetMapping("/download/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Download invoice PDF", description = "Download PDF invoice file for a specific order")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) {
        log.info("Downloading invoice PDF for order: {}", orderId);
        byte[] pdfBytes = invoiceService.getInvoicePdfBytes(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-order-" + orderId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
