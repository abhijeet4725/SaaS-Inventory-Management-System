package com.saasproject.modules.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product DTOs.
 */
public class ProductDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create product request")
    public static class CreateRequest {

        @NotBlank(message = "Name is required")
        @Size(max = 255)
        @Schema(description = "Product name", example = "Widget Pro")
        private String name;

        @Size(max = 1000)
        @Schema(description = "Product description")
        private String description;

        @Size(max = 100)
        @Schema(description = "SKU", example = "WDG-001")
        private String sku;

        @Size(max = 100)
        @Schema(description = "Barcode", example = "1234567890123")
        private String barcode;

        @Size(max = 100)
        @Schema(description = "Category", example = "Electronics")
        private String category;

        @Size(max = 100)
        @Schema(description = "Brand", example = "Acme")
        private String brand;

        @Schema(description = "Unit of measurement", example = "piece")
        private String unit = "piece";

        @DecimalMin(value = "0.00")
        @Schema(description = "Cost price", example = "10.00")
        private BigDecimal costPrice;

        @NotNull(message = "Selling price is required")
        @DecimalMin(value = "0.01")
        @Schema(description = "Selling price", example = "19.99")
        private BigDecimal sellingPrice;

        @DecimalMin(value = "0.00")
        @DecimalMax(value = "100.00")
        @Schema(description = "Tax rate %", example = "10.00")
        private BigDecimal taxRate;

        @Min(0)
        @Schema(description = "Initial stock", example = "100")
        private Integer currentStock = 0;

        @Min(0)
        @Schema(description = "Minimum stock level for alerts", example = "10")
        private Integer minStockLevel = 0;

        @Schema(description = "Track inventory levels", example = "true")
        private boolean trackInventory = true;

        @Schema(description = "This is a service (no physical stock)", example = "false")
        private boolean service = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update product request")
    public static class UpdateRequest {
        private String name;
        private String description;
        private String sku;
        private String barcode;
        private String category;
        private String brand;
        private String unit;
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;
        private BigDecimal taxRate;
        private Integer minStockLevel;
        private Integer maxStockLevel;
        private Integer reorderQuantity;
        private String imageUrl;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Stock update request")
    public static class StockUpdateRequest {

        @NotNull(message = "Quantity is required")
        @Schema(description = "Quantity to add (positive) or remove (negative)", example = "10")
        private Integer quantity;

        @NotBlank(message = "Reason is required")
        @Schema(description = "Reason for adjustment", example = "PURCHASE")
        private String reason;

        @Schema(description = "Notes", example = "Received from supplier")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Product response")
    public static class Response {
        private String id;
        private String name;
        private String description;
        private String sku;
        private String barcode;
        private String category;
        private String brand;
        private String unit;
        private BigDecimal costPrice;
        private BigDecimal sellingPrice;
        private BigDecimal taxRate;
        private BigDecimal priceWithTax;
        private Integer currentStock;
        private Integer minStockLevel;
        private boolean lowStock;
        private boolean active;
        private boolean service;
        private boolean trackInventory;
        private String imageUrl;
        private String createdAt;
        private String updatedAt;
    }
}
