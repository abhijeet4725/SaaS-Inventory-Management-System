package com.saasproject.modules.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Purchase Order DTOs.
 */
public class PurchaseOrderDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create purchase order request")
    public static class CreateRequest {

        @NotBlank(message = "Supplier ID is required")
        @Schema(description = "Supplier ID")
        private String supplierId;

        @Schema(description = "Expected delivery date (yyyy-MM-dd)")
        private String expectedDate;

        @Schema(description = "Shipping cost")
        private BigDecimal shippingCost;

        @Schema(description = "Discount amount")
        private BigDecimal discountAmount;

        @Schema(description = "Shipping address")
        private String shippingAddress;

        @Schema(description = "Notes")
        private String notes;

        @NotEmpty(message = "At least one item is required")
        @Schema(description = "Order items")
        private List<ItemRequest> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "PO item request")
    public static class ItemRequest {

        @Schema(description = "Product ID (optional)")
        private String productId;

        @NotBlank(message = "Product name is required")
        @Schema(description = "Product name")
        private String productName;

        @Schema(description = "Product SKU")
        private String productSku;

        @Schema(description = "Description")
        private String description;

        @Min(1)
        @Schema(description = "Quantity", example = "10")
        private int quantity;

        @NotNull
        @DecimalMin("0.01")
        @Schema(description = "Unit cost", example = "25.00")
        private BigDecimal unitCost;

        @DecimalMin("0.00")
        @DecimalMax("100.00")
        @Schema(description = "Tax rate %", example = "10.00")
        private BigDecimal taxRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Receive items request")
    public static class ReceiveRequest {

        @NotEmpty(message = "At least one item is required")
        @Schema(description = "Items to receive")
        private List<ReceiveItemRequest> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiveItemRequest {
        private String itemId;
        private int quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Purchase order response")
    public static class Response {
        private String id;
        private String poNumber;
        private String supplierId;
        private String supplierName;
        private String supplierEmail;
        private String orderDate;
        private String expectedDate;
        private String receivedDate;
        private String status;
        private List<ItemResponse> items;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal shippingCost;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String shippingAddress;
        private String notes;
        private String approvedBy;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "PO item response")
    public static class ItemResponse {
        private String id;
        private String productId;
        private String productName;
        private String productSku;
        private String description;
        private int quantity;
        private int receivedQuantity;
        private int pendingQuantity;
        private BigDecimal unitCost;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "PO summary for lists")
    public static class Summary {
        private String id;
        private String poNumber;
        private String supplierName;
        private String orderDate;
        private String status;
        private BigDecimal totalAmount;
        private int itemCount;
    }
}
