package com.saasproject.modules.inventory.entity;

import com.saasproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Stock movement entity for tracking inventory changes.
 */
@Entity
@Table(name = "stock_movements", indexes = {
        @Index(name = "idx_stock_movements_tenant", columnList = "tenant_id"),
        @Index(name = "idx_stock_movements_product", columnList = "product_id"),
        @Index(name = "idx_stock_movements_type", columnList = "movement_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "stock_before", nullable = false)
    private Integer stockBefore;

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "notes", length = 500)
    private String notes;

    public enum MovementType {
        PURCHASE, // Stock in from purchase
        SALE, // Stock out from sale
        RETURN, // Customer return
        ADJUSTMENT, // Manual adjustment
        TRANSFER_IN, // Transfer from another location
        TRANSFER_OUT, // Transfer to another location
        DAMAGE, // Damaged goods write-off
        EXPIRED // Expired goods write-off
    }
}
