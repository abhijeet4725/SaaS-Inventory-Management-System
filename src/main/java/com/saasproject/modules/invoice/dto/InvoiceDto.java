package com.saasproject.modules.invoice.dto;

import com.saasproject.modules.invoice.entity.Invoice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Invoice DTOs.
 */
public class InvoiceDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create invoice request")
    public static class CreateRequest {

        @Schema(description = "Customer ID", example = "cust-123")
        private String customerId;

        @Size(max = 255)
        @Schema(description = "Customer name", example = "John Doe")
        private String customerName;

        @Email
        @Schema(description = "Customer email", example = "john@example.com")
        private String customerEmail;

        @Schema(description = "Customer phone", example = "+1234567890")
        private String customerPhone;

        @Schema(description = "Customer address")
        private String customerAddress;

        @NotNull(message = "Invoice date is required")
        @Schema(description = "Invoice date", example = "2024-01-15")
        private LocalDate invoiceDate;

        @Schema(description = "Due date", example = "2024-02-15")
        private LocalDate dueDate;

        @NotEmpty(message = "At least one item is required")
        @Valid
        @Schema(description = "Invoice line items")
        private List<ItemRequest> items;

        @DecimalMin("0.00")
        @Schema(description = "Discount amount", example = "10.00")
        private BigDecimal discountAmount;

        @Schema(description = "Notes")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Invoice item request")
    public static class ItemRequest {

        @Schema(description = "Product ID (optional)")
        private String productId;

        @NotBlank(message = "Product name is required")
        @Schema(description = "Product name", example = "Widget Pro")
        private String productName;

        @Schema(description = "Product SKU", example = "WDG-001")
        private String productSku;

        @Schema(description = "Description")
        private String description;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Quantity", example = "2")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01")
        @Schema(description = "Unit price", example = "19.99")
        private BigDecimal unitPrice;

        @DecimalMin("0.00")
        @DecimalMax("100.00")
        @Schema(description = "Discount %", example = "5.00")
        private BigDecimal discountPercent;

        @DecimalMin("0.00")
        @Schema(description = "Tax rate %", example = "10.00")
        private BigDecimal taxRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Payment request")
    public static class PaymentRequest {

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01")
        @Schema(description = "Payment amount", example = "100.00")
        private BigDecimal amount;

        @NotNull(message = "Payment method is required")
        @Schema(description = "Payment method", example = "CASH")
        private Invoice.PaymentMethod paymentMethod;

        @Schema(description = "Payment reference", example = "TXN123456")
        private String reference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Invoice response")
    public static class Response {
        private String id;
        private String invoiceNumber;
        private String customerId;
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private String status;
        private String invoiceDate;
        private String dueDate;
        private List<ItemResponse> items;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal balanceDue;
        private String paymentMethod;
        private String paymentReference;
        private String paidAt;
        private String notes;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Invoice item response")
    public static class ItemResponse {
        private String id;
        private String productId;
        private String productName;
        private String productSku;
        private String description;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal amount;
    }
}
