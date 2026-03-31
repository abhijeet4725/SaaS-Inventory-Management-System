package com.saasproject.modules.company.entity;

import com.saasproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Company/Tenant entity representing a business using the SaaS platform.
 */
@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_companies_tenant", columnList = "tenant_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "legal_name", length = 255)
    private String legalName;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

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

    // Settings
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "date_format", length = 20)
    @Builder.Default
    private String dateFormat = "yyyy-MM-dd";

    @Column(name = "default_tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal defaultTaxRate = BigDecimal.ZERO;

    @Column(name = "invoice_prefix", length = 10)
    @Builder.Default
    private String invoicePrefix = "INV";

    @Column(name = "invoice_footer", length = 1000)
    private String invoiceFooter;

    // Subscription
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", length = 20)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    public enum SubscriptionTier {
        FREE, STARTER, PROFESSIONAL, ENTERPRISE
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
}
