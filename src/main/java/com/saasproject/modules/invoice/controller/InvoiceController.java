package com.saasproject.modules.invoice.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.common.dto.PageRequestDto;
import com.saasproject.modules.invoice.dto.InvoiceDto;
import com.saasproject.modules.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Invoice controller for billing operations.
 */
@RestController
@RequestMapping("/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice and billing management")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Create invoice", description = "Create a new invoice")
    public ResponseEntity<ApiResponse<InvoiceDto.Response>> createInvoice(
            @Valid @RequestBody InvoiceDto.CreateRequest request) {

        InvoiceDto.Response response = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    @Operation(summary = "List invoices", description = "Get paginated list of invoices")
    public ResponseEntity<ApiResponse<List<InvoiceDto.Response>>> getInvoices(
            @ModelAttribute PageRequestDto pageRequest) {

        Page<InvoiceDto.Response> page = invoiceService.getInvoices(pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice", description = "Get invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceDto.Response>> getInvoice(
            @PathVariable UUID id) {

        InvoiceDto.Response response = invoiceService.getInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Record payment", description = "Record a payment on an invoice")
    public ResponseEntity<ApiResponse<InvoiceDto.Response>> recordPayment(
            @PathVariable UUID id,
            @Valid @RequestBody InvoiceDto.PaymentRequest request) {

        InvoiceDto.Response response = invoiceService.recordPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cancel invoice", description = "Cancel an unpaid invoice")
    public ResponseEntity<ApiResponse<Void>> cancelInvoice(@PathVariable UUID id) {

        invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice cancelled"));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Export PDF", description = "Export invoice as PDF")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID id) {

        byte[] pdfContent = invoiceService.exportPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }
}
