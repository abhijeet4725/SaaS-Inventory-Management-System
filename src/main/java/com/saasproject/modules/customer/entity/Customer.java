package com.saasproject.modules.customer.entity;

import com.saasproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Customer entity for customer management.
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_tenant", columnList = "tenant_id"),
        @Index(name = "idx_customers_code", columnList = "customer_code"),
        @Index(name = "idx_customers_email", columnList = "email"),
        @Index(name = "idx_customers_phone", columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Column(name = "customer_code", length = 50)
    private String customerCode;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "alt_phone", length = 20)
    private String altPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", length = 20)
    @Builder.Default
    private CustomerType customerType = CustomerType.INDIVIDUAL;

    // Company details (for business customers)
    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    // Address
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;

    // Financial
    @Column(name = "credit_limit", precision = 14, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "outstanding_balance", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(name = "total_purchases", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalPurchases = BigDecimal.ZERO;

    @Column(name = "loyalty_points")
    @Builder.Default
    private int loyaltyPoints = 0;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    public enum CustomerType {
        INDIVIDUAL, BUSINESS, WALK_IN
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null)
            sb.append(addressLine1);
        if (addressLine2 != null)
            sb.append(", ").append(addressLine2);
        if (city != null)
            sb.append(", ").append(city);
        if (state != null)
            sb.append(", ").append(state);
        if (postalCode != null)
            sb.append(" ").append(postalCode);
        if (country != null)
            sb.append(", ").append(country);
        return sb.toString();
    }

    public void recordPurchase(BigDecimal amount) {
        this.totalPurchases = this.totalPurchases.add(amount);
    }

    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
    }

    public void redeemLoyaltyPoints(int points) {
        if (points > this.loyaltyPoints) {
            throw new IllegalArgumentException("Insufficient loyalty points");
        }
        this.loyaltyPoints -= points;
    }
}
