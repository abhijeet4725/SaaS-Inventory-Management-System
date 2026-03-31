package com.saasproject.modules.printer.service;

import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.modules.invoice.entity.Invoice;
import com.saasproject.modules.invoice.repository.InvoiceRepository;
import com.saasproject.modules.pos.entity.Cart;
import com.saasproject.modules.pos.repository.CartRepository;
import com.saasproject.modules.printer.dto.PrinterDto;
import com.saasproject.modules.printer.entity.PrintJob;
import com.saasproject.modules.printer.entity.Printer;
import com.saasproject.modules.printer.repository.PrintJobRepository;
import com.saasproject.modules.printer.repository.PrinterRepository;
import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for printer management and print job queue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrinterService {

    private final PrinterRepository printerRepository;
    private final PrintJobRepository printJobRepository;
    private final InvoiceRepository invoiceRepository;
    private final CartRepository cartRepository;
    private final EscPosService escPosService;

    // ===== Printer Management =====

    @Transactional(readOnly = true)
    public List<PrinterDto.PrinterResponse> getAllPrinters() {
        String tenantId = TenantContext.getCurrentTenant();
        return printerRepository.findByTenant(tenantId).stream()
                .map(PrinterDto.PrinterResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrinterDto.PrinterResponse getPrinter(UUID id) {
        Printer printer = getPrinterEntity(id);
        return PrinterDto.PrinterResponse.from(printer);
    }

    @Transactional
    public PrinterDto.PrinterResponse createPrinter(PrinterDto.PrinterRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Printer printer = Printer.builder()
                .name(request.getName())
                .description(request.getDescription())
                .printerType(
                        request.getPrinterType() != null ? request.getPrinterType() : Printer.PrinterType.THERMAL_80MM)
                .connectionType(request.getConnectionType())
                .ipAddress(request.getIpAddress())
                .port(request.getPort() != null ? request.getPort() : 9100)
                .usbPath(request.getUsbPath())
                .paperWidth(request.getPaperWidth() != null ? request.getPaperWidth() : 80)
                .isDefault(request.getIsDefault() != null && request.getIsDefault())
                .active(true)
                .build();
        printer.setTenantId(tenantId);

        // If this is the first printer or marked as default, make it default
        if (printer.getIsDefault() || printerRepository.countByTenant(tenantId) == 0) {
            clearDefaultPrinter(tenantId);
            printer.setIsDefault(true);
        }

        printer = printerRepository.save(printer);
        log.info("Created printer: {} for tenant: {}", printer.getName(), tenantId);

        return PrinterDto.PrinterResponse.from(printer);
    }

    @Transactional
    public PrinterDto.PrinterResponse updatePrinter(UUID id, PrinterDto.PrinterRequest request) {
        Printer printer = getPrinterEntity(id);

        printer.setName(request.getName());
        printer.setDescription(request.getDescription());
        if (request.getPrinterType() != null)
            printer.setPrinterType(request.getPrinterType());
        if (request.getConnectionType() != null)
            printer.setConnectionType(request.getConnectionType());
        printer.setIpAddress(request.getIpAddress());
        if (request.getPort() != null)
            printer.setPort(request.getPort());
        printer.setUsbPath(request.getUsbPath());
        if (request.getPaperWidth() != null)
            printer.setPaperWidth(request.getPaperWidth());

        if (request.getIsDefault() != null && request.getIsDefault()) {
            clearDefaultPrinter(printer.getTenantId());
            printer.setIsDefault(true);
        }

        printer = printerRepository.save(printer);
        return PrinterDto.PrinterResponse.from(printer);
    }

    @Transactional
    public void deletePrinter(UUID id) {
        Printer printer = getPrinterEntity(id);
        printer.setDeleted(true);
        printer.setActive(false);
        printerRepository.save(printer);
        log.info("Deleted printer: {}", id);
    }

    // ===== Print Operations =====

    @Transactional
    public PrinterDto.PrintJobResponse printInvoice(UUID invoiceId, UUID printerId) {
        String tenantId = TenantContext.getCurrentTenant();

        Invoice invoice = invoiceRepository.findByIdAndTenant(invoiceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId.toString()));

        Printer printer = printerId != null
                ? getPrinterEntity(printerId)
                : getDefaultPrinter(tenantId);

        // Create print job
        PrintJob job = PrintJob.builder()
                .printer(printer)
                .jobType(PrintJob.JobType.INVOICE)
                .status(PrintJob.JobStatus.PENDING)
                .referenceType("INVOICE")
                .referenceId(invoice.getInvoiceNumber())
                .build();
        job.setTenantId(tenantId);

        // Generate preview
        List<String> previewLines = escPosService.generateReceiptPreviewLines(invoice, printer.getPaperWidth());
        job.setContentPreview(String.join("\n", previewLines));

        job = printJobRepository.save(job);

        // Execute print async
        executePrintAsync(job.getId(), invoice, printer);

        return PrinterDto.PrintJobResponse.from(job);
    }

    @Transactional
    public PrinterDto.PrintJobResponse printCart(UUID cartId, UUID printerId) {
        String tenantId = TenantContext.getCurrentTenant();

        Cart cart = cartRepository.findByIdAndTenant(cartId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId.toString()));

        Printer printer = printerId != null
                ? getPrinterEntity(printerId)
                : getDefaultPrinter(tenantId);

        PrintJob job = PrintJob.builder()
                .printer(printer)
                .jobType(PrintJob.JobType.RECEIPT)
                .status(PrintJob.JobStatus.PENDING)
                .referenceType("CART")
                .referenceId(cartId.toString())
                .build();
        job.setTenantId(tenantId);

        job = printJobRepository.save(job);

        // Execute print async
        executePrintCartAsync(job.getId(), cart, printer);

        return PrinterDto.PrintJobResponse.from(job);
    }

    @Transactional
    public PrinterDto.PrintJobResponse testPrint(UUID printerId, String message) {
        String tenantId = TenantContext.getCurrentTenant();

        Printer printer = printerId != null
                ? getPrinterEntity(printerId)
                : getDefaultPrinter(tenantId);

        PrintJob job = PrintJob.builder()
                .printer(printer)
                .jobType(PrintJob.JobType.RECEIPT)
                .status(PrintJob.JobStatus.PENDING)
                .referenceType("TEST")
                .referenceId("test-" + System.currentTimeMillis())
                .contentPreview(message != null ? message : "Printer test")
                .build();
        job.setTenantId(tenantId);

        job = printJobRepository.save(job);

        executeTestPrintAsync(job.getId(), message, printer);

        return PrinterDto.PrintJobResponse.from(job);
    }

    // ===== Print Job Operations =====

    @Transactional(readOnly = true)
    public Page<PrinterDto.PrintJobResponse> getPrintJobs(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return printJobRepository.findByTenant(tenantId, pageable)
                .map(PrinterDto.PrintJobResponse::from);
    }

    @Transactional(readOnly = true)
    public PrinterDto.PrintJobResponse getPrintJob(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        PrintJob job = printJobRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", id.toString()));
        return PrinterDto.PrintJobResponse.from(job);
    }

    @Transactional
    public PrinterDto.PrintJobResponse retryPrintJob(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        PrintJob job = printJobRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", id.toString()));

        if (!job.canRetry()) {
            throw new IllegalStateException(
                    "Job cannot be retried. Status: " + job.getStatus() + ", Retries: " + job.getRetryCount());
        }

        job.setStatus(PrintJob.JobStatus.PENDING);
        job = printJobRepository.save(job);

        // Re-execute based on job type
        if ("INVOICE".equals(job.getReferenceType()) && job.getReferenceId() != null) {
            Invoice invoice = invoiceRepository
                    .findByInvoiceNumberAndTenantIdAndDeletedFalse(job.getReferenceId(), tenantId)
                    .orElse(null);
            if (invoice != null && job.getPrinter() != null) {
                executePrintAsync(job.getId(), invoice, job.getPrinter());
            }
        }

        return PrinterDto.PrintJobResponse.from(job);
    }

    // ===== Preview =====

    @Transactional(readOnly = true)
    public PrinterDto.ReceiptPreview getInvoicePreview(UUID invoiceId) {
        String tenantId = TenantContext.getCurrentTenant();

        Invoice invoice = invoiceRepository.findByIdAndTenant(invoiceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId.toString()));

        Printer printer = printerRepository.findDefaultByTenant(tenantId)
                .orElse(null);
        int paperWidth = printer != null ? printer.getPaperWidth() : 80;

        List<String> lines = escPosService.generateReceiptPreviewLines(invoice, paperWidth);

        return PrinterDto.ReceiptPreview.builder()
                .lines(lines)
                .textContent(String.join("\n", lines))
                .totalLines(lines.size())
                .paperWidth(paperWidth)
                .build();
    }

    // ===== Async Print Execution =====

    @Async
    protected void executePrintAsync(UUID jobId, Invoice invoice, Printer printer) {
        try {
            log.info("Executing print job: {} for invoice: {}", jobId, invoice.getInvoiceNumber());

            byte[] data = escPosService.generateInvoiceReceipt(invoice, printer.getPaperWidth());

            if (printer.getConnectionType() == Printer.ConnectionType.NETWORK) {
                escPosService.sendToNetworkPrinter(printer.getIpAddress(), printer.getPort(), data);
            } else {
                log.warn("USB/Bluetooth printing not yet implemented");
            }

            updateJobStatus(jobId, PrintJob.JobStatus.COMPLETED, null);
        } catch (Exception e) {
            log.error("Print failed for job {}: {}", jobId, e.getMessage());
            updateJobStatus(jobId, PrintJob.JobStatus.FAILED, e.getMessage());
        }
    }

    @Async
    protected void executePrintCartAsync(UUID jobId, Cart cart, Printer printer) {
        try {
            log.info("Executing print job: {} for cart", jobId);

            byte[] data = escPosService.generateCartReceipt(cart, printer.getPaperWidth());

            if (printer.getConnectionType() == Printer.ConnectionType.NETWORK) {
                escPosService.sendToNetworkPrinter(printer.getIpAddress(), printer.getPort(), data);
            }

            updateJobStatus(jobId, PrintJob.JobStatus.COMPLETED, null);
        } catch (Exception e) {
            log.error("Print failed for job {}: {}", jobId, e.getMessage());
            updateJobStatus(jobId, PrintJob.JobStatus.FAILED, e.getMessage());
        }
    }

    @Async
    protected void executeTestPrintAsync(UUID jobId, String message, Printer printer) {
        try {
            log.info("Executing test print job: {}", jobId);

            byte[] data = escPosService.generateTestPrint(message, printer.getPaperWidth());

            if (printer.getConnectionType() == Printer.ConnectionType.NETWORK) {
                escPosService.sendToNetworkPrinter(printer.getIpAddress(), printer.getPort(), data);
            }

            updateJobStatus(jobId, PrintJob.JobStatus.COMPLETED, null);
        } catch (Exception e) {
            log.error("Test print failed for job {}: {}", jobId, e.getMessage());
            updateJobStatus(jobId, PrintJob.JobStatus.FAILED, e.getMessage());
        }
    }

    // ===== Helper Methods =====

    private Printer getPrinterEntity(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        return printerRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Printer", id.toString()));
    }

    private Printer getDefaultPrinter(String tenantId) {
        return printerRepository.findDefaultByTenant(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Printer", "default", "not configured"));
    }

    private void clearDefaultPrinter(String tenantId) {
        printerRepository.findDefaultByTenant(tenantId)
                .ifPresent(p -> {
                    p.setIsDefault(false);
                    printerRepository.save(p);
                });
    }

    @Transactional
    protected void updateJobStatus(UUID jobId, PrintJob.JobStatus status, String errorMessage) {
        printJobRepository.findById(jobId).ifPresent(job -> {
            if (status == PrintJob.JobStatus.COMPLETED) {
                job.complete();
            } else if (status == PrintJob.JobStatus.FAILED) {
                job.fail(errorMessage);
            } else {
                job.setStatus(status);
            }
            printJobRepository.save(job);
        });
    }

    // ===== System Printer Discovery =====

    /**
     * Discover USB/local printers available on the server.
     */
    public List<PrinterDto.SystemPrinterInfo> discoverSystemPrinters() {
        log.info("Discovering system printers");
        return escPosService.discoverSystemPrinters().stream()
                .map(sp -> PrinterDto.SystemPrinterInfo.builder()
                        .name(sp.getName())
                        .isDefault(sp.isDefault())
                        .supportsRaw(sp.isSupportsRaw())
                        .connectionType(sp.getConnectionType())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get the default system printer.
     */
    public PrinterDto.SystemPrinterInfo getDefaultSystemPrinter() {
        EscPosService.SystemPrinter sp = escPosService.getDefaultSystemPrinter();
        if (sp == null) {
            return null;
        }
        return PrinterDto.SystemPrinterInfo.builder()
                .name(sp.getName())
                .isDefault(sp.isDefault())
                .supportsRaw(sp.isSupportsRaw())
                .connectionType(sp.getConnectionType())
                .build();
    }

    /**
     * Check if a specific printer is available on the system.
     */
    public boolean isPrinterAvailable(String printerName) {
        return escPosService.isPrinterAvailable(printerName);
    }
}
