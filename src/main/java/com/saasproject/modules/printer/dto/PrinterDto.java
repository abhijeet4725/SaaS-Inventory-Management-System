package com.saasproject.modules.printer.dto;

import com.saasproject.modules.printer.entity.PrintJob;
import com.saasproject.modules.printer.entity.Printer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTOs for Printer module.
 */
public class PrinterDto {

    // ===== Printer DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrinterRequest {
        @NotBlank(message = "Printer name is required")
        private String name;

        private String description;

        private Printer.PrinterType printerType;

        @NotNull(message = "Connection type is required")
        private Printer.ConnectionType connectionType;

        private String ipAddress;
        private Integer port;
        private String usbPath;
        private Integer paperWidth;
        private Boolean isDefault;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrinterResponse {
        private UUID id;
        private String name;
        private String description;
        private Printer.PrinterType printerType;
        private Printer.ConnectionType connectionType;
        private String ipAddress;
        private Integer port;
        private String usbPath;
        private Integer paperWidth;
        private Boolean isDefault;
        private Boolean active;
        private LocalDateTime createdAt;

        public static PrinterResponse from(Printer printer) {
            return PrinterResponse.builder()
                    .id(printer.getId())
                    .name(printer.getName())
                    .description(printer.getDescription())
                    .printerType(printer.getPrinterType())
                    .connectionType(printer.getConnectionType())
                    .ipAddress(printer.getIpAddress())
                    .port(printer.getPort())
                    .usbPath(printer.getUsbPath())
                    .paperWidth(printer.getPaperWidth())
                    .isDefault(printer.getIsDefault())
                    .active(printer.getActive())
                    .createdAt(printer.getCreatedAt())
                    .build();
        }
    }

    // ===== Print Job DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintReceiptRequest {
        private UUID printerId; // Optional, uses default printer if not specified
        private UUID invoiceId; // For invoice printing
        private UUID cartId; // For cart/receipt printing
        private Integer copies;
        private Boolean openCashDrawer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintJobResponse {
        private UUID id;
        private UUID printerId;
        private String printerName;
        private PrintJob.JobType jobType;
        private PrintJob.JobStatus status;
        private String referenceType;
        private String referenceId;
        private String contentPreview;
        private String errorMessage;
        private Integer retryCount;
        private LocalDateTime createdAt;
        private LocalDateTime printedAt;

        public static PrintJobResponse from(PrintJob job) {
            return PrintJobResponse.builder()
                    .id(job.getId())
                    .printerId(job.getPrinter() != null ? job.getPrinter().getId() : null)
                    .printerName(job.getPrinter() != null ? job.getPrinter().getName() : null)
                    .jobType(job.getJobType())
                    .status(job.getStatus())
                    .referenceType(job.getReferenceType())
                    .referenceId(job.getReferenceId())
                    .contentPreview(job.getContentPreview())
                    .errorMessage(job.getErrorMessage())
                    .retryCount(job.getRetryCount())
                    .createdAt(job.getCreatedAt())
                    .printedAt(job.getPrintedAt())
                    .build();
        }
    }

    // ===== Receipt Preview =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptPreview {
        private String textContent; // Plain text preview
        private List<String> lines; // Lines for display
        private int totalLines;
        private int paperWidth; // mm
    }

    // ===== Print Test =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestPrintRequest {
        private UUID printerId;
        private String message;
    }

    // ===== System Printer Discovery =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemPrinterInfo {
        private String name; // System printer name
        private boolean isDefault; // Is the default system printer
        private boolean supportsRaw; // Supports raw printing (thermal printers)
        private String connectionType; // USB/LOCAL
    }
}
