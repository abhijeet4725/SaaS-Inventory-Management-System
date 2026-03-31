package com.saasproject.modules.printer.entity;

import com.saasproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Print job entity for tracking print queue.
 */
@Entity
@Table(name = "print_jobs", indexes = {
        @Index(name = "idx_print_jobs_tenant", columnList = "tenantId"),
        @Index(name = "idx_print_jobs_status", columnList = "tenantId, status"),
        @Index(name = "idx_print_jobs_printer", columnList = "printer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintJob extends BaseEntity {

    public enum JobType {
        RECEIPT, // POS receipt
        INVOICE, // Invoice printout
        BARCODE, // Barcode/label print
        REPORT // Report printout
    }

    public enum JobStatus {
        PENDING, // Waiting to print
        PRINTING, // Currently printing
        COMPLETED, // Successfully printed
        FAILED, // Print failed
        CANCELLED // Cancelled by user
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "printer_id")
    private Printer printer;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "reference_type", length = 50)
    private String referenceType; // e.g., "INVOICE", "CART"

    @Column(name = "reference_id", length = 100)
    private String referenceId; // e.g., invoice ID or cart ID

    @Column(name = "content_preview", columnDefinition = "TEXT")
    private String contentPreview; // Text preview of what will be printed

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column
    @Builder.Default
    private Integer priority = 5; // 1-10, lower = higher priority

    @Column(name = "printed_at")
    private LocalDateTime printedAt;

    /**
     * Mark job as completed.
     */
    public void complete() {
        this.status = JobStatus.COMPLETED;
        this.printedAt = LocalDateTime.now();
    }

    /**
     * Mark job as failed with error message.
     */
    public void fail(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * Check if job can be retried (max 3 retries).
     */
    public boolean canRetry() {
        return status == JobStatus.FAILED && retryCount < 3;
    }
}
