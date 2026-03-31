package com.saasproject.modules.pos.entity;

import com.saasproject.common.entity.BaseEntity;
import com.saasproject.modules.invoice.entity.Invoice;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * POS Cart entity for checkout process.
 */
@Entity
@Table(name = "pos_carts", indexes = {
        @Index(name = "idx_carts_tenant", columnList = "tenant_id"),
        @Index(name = "idx_carts_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "subtotal", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private Invoice.PaymentMethod paymentMethod;

    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;

    @Column(name = "cashier_id", length = 100)
    private String cashierId;

    @Column(name = "register_id", length = 50)
    private String registerId;

    public enum CartStatus {
        ACTIVE, CHECKED_OUT, COMPLETED, ABANDONED, CANCELLED, VOID
    }

    // ===== Helper Methods =====

    public void addItem(CartItem item) {
        // Check if product already in cart
        CartItem existing = items.stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
            existing.calculateAmount();
        } else {
            items.add(item);
            item.setCart(this);
        }

        recalculateTotals();
    }

    public void removeItem(UUID productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotals();
    }

    public void updateItemQuantity(UUID productId, int quantity) {
        items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        items.remove(item);
                    } else {
                        item.setQuantity(quantity);
                        item.calculateAmount();
                    }
                });

        recalculateTotals();
    }

    public void recalculateTotals() {
        this.subtotal = items.stream()
                .map(CartItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.taxAmount = items.stream()
                .map(CartItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
    }

    public void checkout(Invoice.PaymentMethod method, String cashierId) {
        this.status = CartStatus.CHECKED_OUT;
        this.paymentMethod = method;
        this.checkedOutAt = LocalDateTime.now();
        this.cashierId = cashierId;
    }

    public void clear() {
        this.items.clear();
        recalculateTotals();
    }
}
