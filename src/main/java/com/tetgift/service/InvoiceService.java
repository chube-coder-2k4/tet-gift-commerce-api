package com.tetgift.service;

import com.tetgift.dto.response.InvoiceResponse;

public interface InvoiceService {

    /**
     * Generate invoice for an order after payment success.
     * Creates PDF, uploads to Cloudinary, saves InvoiceEntity.
     */
    InvoiceResponse generateInvoice(Long orderId);

    /**
     * Get invoice by order ID.
     */
    InvoiceResponse getInvoiceByOrderId(Long orderId);

    /**
     * Get raw PDF bytes for download.
     */
    byte[] getInvoicePdfBytes(Long orderId);
}
