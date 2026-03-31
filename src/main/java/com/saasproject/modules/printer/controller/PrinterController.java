package com.saasproject.modules.printer.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.modules.printer.dto.PrinterDto;
import com.saasproject.modules.printer.service.PrinterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for printer management and print operations.
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Printer", description = "Printer management and print operations")
public class PrinterController {

    private final PrinterService printerService;

    // ===== Printer Management =====

    @GetMapping("/printers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "List all printers", description = "Get all registered printers for current tenant")
    public ResponseEntity<ApiResponse<List<PrinterDto.PrinterResponse>>> getAllPrinters() {
        List<PrinterDto.PrinterResponse> printers = printerService.getAllPrinters();
        return ResponseEntity.ok(ApiResponse.success(printers));
    }

    @GetMapping("/printers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get printer details", description = "Get details of a specific printer")
    public ResponseEntity<ApiResponse<PrinterDto.PrinterResponse>> getPrinter(@PathVariable UUID id) {
        PrinterDto.PrinterResponse printer = printerService.getPrinter(id);
        return ResponseEntity.ok(ApiResponse.success(printer));
    }

    @PostMapping("/printers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Register printer", description = "Register a new printer")
    public ResponseEntity<ApiResponse<PrinterDto.PrinterResponse>> createPrinter(
            @Valid @RequestBody PrinterDto.PrinterRequest request) {
        PrinterDto.PrinterResponse printer = printerService.createPrinter(request);
        return ResponseEntity.ok(ApiResponse.created(printer));
    }

    @PutMapping("/printers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update printer", description = "Update printer settings")
    public ResponseEntity<ApiResponse<PrinterDto.PrinterResponse>> updatePrinter(
            @PathVariable UUID id,
            @Valid @RequestBody PrinterDto.PrinterRequest request) {
        PrinterDto.PrinterResponse printer = printerService.updatePrinter(id, request);
        return ResponseEntity.ok(ApiResponse.success(printer));
    }

    @DeleteMapping("/printers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete printer", description = "Remove a printer")
    public ResponseEntity<ApiResponse<Void>> deletePrinter(@PathVariable UUID id) {
        printerService.deletePrinter(id);
        return ResponseEntity.ok(ApiResponse.success("Printer deleted successfully", null));
    }

    // ===== Print Operations =====

    @PostMapping("/print/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Print invoice", description = "Print an invoice receipt")
    public ResponseEntity<ApiResponse<PrinterDto.PrintJobResponse>> printInvoice(
            @PathVariable UUID invoiceId,
            @RequestParam(required = false) UUID printerId) {
        PrinterDto.PrintJobResponse job = printerService.printInvoice(invoiceId, printerId);
        return ResponseEntity.ok(ApiResponse.success("Print job queued", job));
    }

    @PostMapping("/print/cart/{cartId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Print receipt", description = "Print a POS cart receipt")
    public ResponseEntity<ApiResponse<PrinterDto.PrintJobResponse>> printCart(
            @PathVariable UUID cartId,
            @RequestParam(required = false) UUID printerId) {
        PrinterDto.PrintJobResponse job = printerService.printCart(cartId, printerId);
        return ResponseEntity.ok(ApiResponse.success("Print job queued", job));
    }

    @PostMapping("/print/test")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Test print", description = "Send a test print to verify printer")
    public ResponseEntity<ApiResponse<PrinterDto.PrintJobResponse>> testPrint(
            @RequestBody PrinterDto.TestPrintRequest request) {
        PrinterDto.PrintJobResponse job = printerService.testPrint(request.getPrinterId(), request.getMessage());
        return ResponseEntity.ok(ApiResponse.success("Test print sent", job));
    }

    // ===== Print Jobs =====

    @GetMapping("/print/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "List print jobs", description = "Get print job history")
    public ResponseEntity<ApiResponse<Page<PrinterDto.PrintJobResponse>>> getPrintJobs(Pageable pageable) {
        Page<PrinterDto.PrintJobResponse> jobs = printerService.getPrintJobs(pageable);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/print/jobs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Get job status", description = "Get status of a print job")
    public ResponseEntity<ApiResponse<PrinterDto.PrintJobResponse>> getPrintJob(@PathVariable UUID id) {
        PrinterDto.PrintJobResponse job = printerService.getPrintJob(id);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    @PostMapping("/print/jobs/{id}/retry")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Retry print job", description = "Retry a failed print job")
    public ResponseEntity<ApiResponse<PrinterDto.PrintJobResponse>> retryPrintJob(@PathVariable UUID id) {
        PrinterDto.PrintJobResponse job = printerService.retryPrintJob(id);
        return ResponseEntity.ok(ApiResponse.success("Print job retrying", job));
    }

    // ===== Preview =====

    @GetMapping("/print/preview/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Preview invoice receipt", description = "Get text preview of invoice receipt")
    public ResponseEntity<ApiResponse<PrinterDto.ReceiptPreview>> previewInvoice(@PathVariable UUID invoiceId) {
        PrinterDto.ReceiptPreview preview = printerService.getInvoicePreview(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(preview));
    }

    // ===== System Printer Discovery =====

    @GetMapping("/printers/system")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    @Operation(summary = "Discover system printers", description = "Get list of USB/local printers available on server")
    public ResponseEntity<ApiResponse<List<PrinterDto.SystemPrinterInfo>>> discoverSystemPrinters() {
        List<PrinterDto.SystemPrinterInfo> printers = printerService.discoverSystemPrinters();
        return ResponseEntity.ok(ApiResponse.success(printers));
    }

    @GetMapping("/printers/system/default")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get default system printer", description = "Get the default system printer")
    public ResponseEntity<ApiResponse<PrinterDto.SystemPrinterInfo>> getDefaultSystemPrinter() {
        PrinterDto.SystemPrinterInfo printer = printerService.getDefaultSystemPrinter();
        if (printer == null) {
            return ResponseEntity.ok(ApiResponse.success("No default printer found", null));
        }
        return ResponseEntity.ok(ApiResponse.success(printer));
    }

    @GetMapping("/printers/system/check/{printerName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Check printer availability", description = "Check if a specific printer is available")
    public ResponseEntity<ApiResponse<Boolean>> checkPrinterAvailable(@PathVariable String printerName) {
        boolean available = printerService.isPrinterAvailable(printerName);
        return ResponseEntity.ok(ApiResponse.success(available));
    }
}
