package com.tetgift.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tetgift.dto.response.InvoiceResponse;
import com.tetgift.enums.OrderStatus;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.entity.InvoiceEntity;
import com.tetgift.model.entity.OrderEntity;
import com.tetgift.model.entity.OrderItemEntity;
import com.tetgift.repository.jpa.InvoiceRepository;
import com.tetgift.repository.jpa.OrderRepository;
import com.tetgift.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final TemplateEngine templateEngine;
    private final Cloudinary cloudinary;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    @Transactional
    public InvoiceResponse generateInvoice(Long orderId) {
        // Check if invoice already exists
        if (invoiceRepository.existsByOrderId(orderId)) {
            log.info("Invoice already exists for order: {}", orderId);
            return getInvoiceByOrderId(orderId);
        }

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        // Only generate for PAID orders
        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidDataException("Cannot generate invoice for unpaid order. Status: " + order.getStatus());
        }

        // Generate invoice number: INV-YYYYMMDD-{orderId}
        String invoiceNumber = generateInvoiceNumber(order);

        // Build PDF bytes
        byte[] pdfBytes = buildPdf(order, invoiceNumber);

        // Upload to Cloudinary
        String pdfUrl = null;
        String publicId = null;
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(pdfBytes, ObjectUtils.asMap(
                    "folder", "invoices",
                    "resource_type", "raw",
                    "public_id", invoiceNumber,
                    "format", "pdf"
            ));
            pdfUrl = (String) uploadResult.get("secure_url");
            publicId = (String) uploadResult.get("public_id");
            log.info("Invoice PDF uploaded to Cloudinary: {}", pdfUrl);
        } catch (Exception e) {
            log.error("Failed to upload invoice to Cloudinary", e);
            throw new InvalidDataException("Failed to upload invoice PDF: " + e.getMessage());
        }

        // Calculate subtotal
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Save entity
        InvoiceEntity invoice = InvoiceEntity.builder()
                .invoiceNumber(invoiceNumber)
                .order(order)
                .companyName(order.getVatCompanyName())
                .taxCode(order.getVatTaxCode())
                .companyPhone(order.getVatPhone())
                .companyAddress(order.getVatAddress())
                .subtotal(subtotal)
                .tierDiscountAmount(order.getTierDiscountAmount() != null ? order.getTierDiscountAmount() : BigDecimal.ZERO)
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO)
                .totalAmount(order.getTotalAmount())
                .pdfUrl(pdfUrl)
                .pdfPublicId(publicId)
                .issuedAt(LocalDateTime.now())
                .build();

        InvoiceEntity saved = invoiceRepository.save(invoice);
        log.info("Invoice created: {} for order: {}", invoiceNumber, orderId);

        return toResponse(saved);
    }

    @Override
    public InvoiceResponse getInvoiceByOrderId(Long orderId) {
        InvoiceEntity invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for order: " + orderId));
        return toResponse(invoice);
    }

    @Override
    public byte[] getInvoicePdfBytes(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidDataException("Cannot generate invoice for unpaid order");
        }

        // Check if invoice exists to get invoice number, otherwise generate new
        String invoiceNumber;
        Optional<InvoiceEntity> existingInvoice = invoiceRepository.findByOrderId(orderId);
        if (existingInvoice.isPresent()) {
            invoiceNumber = existingInvoice.get().getInvoiceNumber();
        } else {
            invoiceNumber = generateInvoiceNumber(order);
        }

        return buildPdf(order, invoiceNumber);
    }

    // ==================== PRIVATE HELPERS ====================

    private String generateInvoiceNumber(OrderEntity order) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "INV-" + datePart + "-" + String.format("%05d", order.getId());
    }

    private byte[] buildPdf(OrderEntity order, String invoiceNumber) {
        // Prepare template context
        Context ctx = new Context(Locale.forLanguageTag("vi"));

        ctx.setVariable("invoiceNumber", invoiceNumber);
        ctx.setVariable("orderId", order.getId());
        ctx.setVariable("issuedDate", LocalDateTime.now().format(DATE_FMT));

        // Company/VAT info
        ctx.setVariable("companyName", order.getVatCompanyName());
        ctx.setVariable("taxCode", order.getVatTaxCode());
        ctx.setVariable("companyPhone", order.getVatPhone());
        ctx.setVariable("companyAddress", order.getVatAddress());

        // Delivery info
        ctx.setVariable("receiverName", order.getReceiverName());
        ctx.setVariable("receiverPhone", order.getReceiverPhone());
        ctx.setVariable("shippingAddress", order.getShippingAddress());

        // Items
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemEntity item : order.getOrderItems()) {
            String name = "PRODUCT".equals(item.getItemType()) && item.getProduct() != null
                    ? item.getProduct().getName()
                    : (item.getBundle() != null ? item.getBundle().getName() : "Unknown");

            BigDecimal itemSubtotal = item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", name);
            row.put("type", item.getItemType());
            row.put("priceFormatted", nf.format(item.getPriceSnapshot()) + " ₫");
            row.put("quantity", item.getQuantity());
            row.put("subtotalFormatted", nf.format(itemSubtotal) + " ₫");
            items.add(row);
        }

        ctx.setVariable("items", items);
        ctx.setVariable("subtotalFormatted", nf.format(subtotal) + " ₫");

        // Discounts
        Integer tierPercent = order.getTierDiscountPercent();
        ctx.setVariable("tierDiscountPercent", tierPercent);
        BigDecimal tierDiscount = order.getTierDiscountAmount() != null ? order.getTierDiscountAmount() : BigDecimal.ZERO;
        ctx.setVariable("tierDiscountFormatted", nf.format(tierDiscount) + " ₫");

        ctx.setVariable("discountCode", order.getDiscountCode());
        BigDecimal discountAmt = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        ctx.setVariable("discountFormatted", nf.format(discountAmt) + " ₫");

        // Total
        ctx.setVariable("totalFormatted", nf.format(order.getTotalAmount()) + " ₫");

        // Render HTML
        String html = templateEngine.process("invoice", ctx);

        // Convert HTML to PDF
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            
            // Ép IText phải đọc thư viện font Unicode Tiếng Việt
            org.springframework.core.io.ClassPathResource fontResource = new org.springframework.core.io.ClassPathResource("fonts/Roboto-Regular.ttf");
            renderer.getFontResolver().addFont(fontResource.getURL().toString(), "Identity-H", true);

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF", e);
            throw new InvalidDataException("Failed to generate invoice PDF: " + e.getMessage());
        }
    }

    private InvoiceResponse toResponse(InvoiceEntity invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(invoice.getOrder().getId())
                .companyName(invoice.getCompanyName())
                .taxCode(invoice.getTaxCode())
                .companyPhone(invoice.getCompanyPhone())
                .companyAddress(invoice.getCompanyAddress())
                .subtotal(invoice.getSubtotal())
                .tierDiscountAmount(invoice.getTierDiscountAmount())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .pdfUrl(invoice.getPdfUrl())
                .issuedAt(invoice.getIssuedAt())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
