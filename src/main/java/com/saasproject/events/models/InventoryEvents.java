package com.saasproject.events.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Inventory-related domain events.
 */
public class InventoryEvents {

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ProductCreatedEvent extends DomainEvent {
        private UUID productId;
        private String productName;
        private String sku;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class StockUpdatedEvent extends DomainEvent {
        private UUID productId;
        private String productName;
        private int previousStock;
        private int newStock;
        private int change;
        private String reason;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LowStockAlertEvent extends DomainEvent {
        private UUID productId;
        private String productName;
        private int currentStock;
        private int minStockLevel;
    }
}
