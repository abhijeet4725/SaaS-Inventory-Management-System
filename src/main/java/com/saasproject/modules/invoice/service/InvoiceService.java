package com.saasproject.modules.invoice.service;

import com.saasproject.common.exceptions.BusinessException;
import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.common.service.PdfGeneratorService;
import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.invoice.dto.InvoiceDto;
import com.saasproject.modules.invoice.entity.Invoice;
import com.saasproject.modules.invoice.entity.InvoiceItem;
import com.saasproject.modules.invoice.repository.InvoiceRepository;
import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Invoice service for creating and managing invoices.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PdfGeneratorService pdfGeneratorService;

    /**
     * Create a new invoice.
     */
    @Transactional
    public InvoiceDto.Response createInvoice(InvoiceDto.CreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating invoice for tenant: {}", tenantId);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber(tenantId))
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .customerAddress(request.getCustomerAddress())
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .discountAmount(request.getDiscountAmount())
                .notes(request.getNotes())
                .status(Invoice.InvoiceStatus.PENDING)
                .build();
        invoice.setTenantId(tenantId);

        // Add items
        for (InvoiceDto.ItemRequest itemRequest : request.getItems()) {
            InvoiceItem item = InvoiceItem.builder()
                    .productId(itemRequest.getProductId() != null ? UUID.fromString(itemRequest.getProductId()) : null)
                    .productName(itemRequest.getProductName())
                    .productSku(itemRequest.getProductSku())
                    .description(itemRequest.getDescription())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .discountPercent(itemRequest.getDiscountPercent())
                    .taxRate(itemRequest.getTaxRate())
                    .build();

            item.calculateAmount();
            invoice.addItem(item);
        }

        invoice.recalculateTotals();
        invoice = invoiceRepository.save(invoice);

        log.info("Invoice created: {}", invoice.getInvoiceNumber());
        return mapToResponse(invoice);
    }

    /**
     * Get invoice by ID.
     */
    @Transactional(readOnly = true)
    public InvoiceDto.Response getInvoice(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();

        Invoice invoice = invoiceRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id.toString()));

        return mapToResponse(invoice);
    }

    /**
     * Get invoices with pagination.
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDto.Response> getInvoices(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return invoiceRepository.findByTenant(tenantId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Record payment on invoice.
     */
    @Transactional
    public InvoiceDto.Response recordPayment(UUID id, InvoiceDto.PaymentRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Recording payment for invoice: {}", id);

        Invoice invoice = invoiceRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id.toString()));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new BusinessException("ALREADY_PAID", "Invoice is already fully paid");
        }

        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new BusinessException("INVOICE_CANCELLED", "Cannot pay a cancelled invoice");
        }

        invoice.recordPayment(request.getAmount(), request.getPaymentMethod(), request.getReference());
        invoice = invoiceRepository.save(invoice);

        log.info("Payment recorded for invoice: {}. Status: {}",
                invoice.getInvoiceNumber(), invoice.getStatus());

        return mapToResponse(invoice);
    }

    /**
     * Cancel invoice.
     */
    @Transactional
    public void cancelInvoice(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Cancelling invoice: {}", id);

        Invoice invoice = invoiceRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id.toString()));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new BusinessException("CANNOT_CANCEL", "Cannot cancel a paid invoice");
        }

        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);

        log.info("Invoice cancelled: {}", invoice.getInvoiceNumber());
    }

    /**
     * Export invoice as PDF.
     */
    public byte[] exportPdf(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        Invoice invoice = invoiceRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id.toString()));

        try {
            log.info("Exporting invoice {} as PDF", invoice.getInvoiceNumber());
            return pdfGeneratorService.generateInvoicePdf(invoice);
        } catch (Exception e) {
            log.error("Failed to generate PDF for invoice {}: {}", id, e.getMessage());
            throw new BusinessException("Failed to generate PDF: " + e.getMessage());
        }
    }

    // ===== Private Methods =====

    private String generateInvoiceNumber(String tenantId) {
        return CommonUtils.generateNumber("INV");
    }

    private InvoiceDto.Response mapToResponse(Invoice invoice) {
        return InvoiceDto.Response.builder()
                .id(invoice.getId().toString())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerId(invoice.getCustomerId())
                .customerName(invoice.getCustomerName())
                .customerEmail(invoice.getCustomerEmail())
                .customerPhone(invoice.getCustomerPhone())
                .status(invoice.getStatus().name())
                .invoiceDate(CommonUtils.formatDate(invoice.getInvoiceDate().atStartOfDay()))
                .dueDate(invoice.getDueDate() != null ? CommonUtils.formatDate(invoice.getDueDate().atStartOfDay())
                        : null)
                .items(invoice.getItems().stream()
                        .map(this::mapItemToResponse)
                        .collect(Collectors.toList()))
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .balanceDue(invoice.getBalanceDue())
                .paymentMethod(invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().name() : null)
                .paymentReference(invoice.getPaymentReference())
                .paidAt(CommonUtils.formatDateTime(invoice.getPaidAt()))
                .notes(invoice.getNotes())
                .createdAt(CommonUtils.formatDateTime(invoice.getCreatedAt()))
                .build();
    }

    private InvoiceDto.ItemResponse mapItemToResponse(InvoiceItem item) {
        return InvoiceDto.ItemResponse.builder()
                .id(item.getId().toString())
                .productId(item.getProductId() != null ? item.getProductId().toString() : null)
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountPercent(item.getDiscountPercent())
                .taxRate(item.getTaxRate())
                .taxAmount(item.getTaxAmount())
                .amount(item.getAmount())
                .build();
    }
}
