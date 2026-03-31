package com.saasproject.modules.inventory.entity;

import com.saasproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Product entity for inventory management.
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_tenant", columnList = "tenant_id"),
        @Index(name = "idx_products_sku", columnList = "sku"),
        @Index(name = "idx_products_barcode", columnList = "barcode")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "unit", length = 50)
    @Builder.Default
    private String unit = "piece";

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "current_stock", nullable = false)
    @Builder.Default
    private Integer currentStock = 0;

    @Column(name = "min_stock_level")
    @Builder.Default
    private Integer minStockLevel = 0;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_service", nullable = false)
    @Builder.Default
    private boolean service = false;

    @Column(name = "track_inventory", nullable = false)
    @Builder.Default
    private boolean trackInventory = true;

    // ===== Helper Methods =====

    public boolean isLowStock() {
        return trackInventory && currentStock <= minStockLevel;
    }

    public void adjustStock(int quantity) {
        if (trackInventory) {
            this.currentStock += quantity;
        }
    }

    public BigDecimal calculateTax() {
        if (taxRate == null || sellingPrice == null) {
            return BigDecimal.ZERO;
        }
        return sellingPrice.multiply(taxRate).divide(BigDecimal.valueOf(100));
    }

    public BigDecimal getPriceWithTax() {
        return sellingPrice.add(calculateTax());
    }
}
