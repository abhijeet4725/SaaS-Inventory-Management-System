package com.saasproject.modules.supplier.entity;

import com.saasproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Supplier entity for vendor/supplier management.
 */
@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_suppliers_tenant", columnList = "tenant_id"),
        @Index(name = "idx_suppliers_code", columnList = "supplier_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @Column(name = "supplier_code", length = 50)
    private String supplierCode;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "contact_person", length = 255)
    private String contactPerson;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "alt_phone", length = 20)
    private String altPhone;

    @Column(name = "website", length = 255)
    private String website;

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
    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Column(name = "payment_terms", length = 100)
    @Builder.Default
    private String paymentTerms = "Net 30";

    @Column(name = "credit_limit", precision = 14, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "outstanding_balance", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(name = "bank_name", length = 255)
    private String bankName;

    @Column(name = "bank_account", length = 100)
    private String bankAccount;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

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
}
