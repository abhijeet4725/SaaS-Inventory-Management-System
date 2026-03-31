package com.saasproject.modules.company.dto;

import com.saasproject.modules.company.entity.Company;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Company DTOs.
 */
public class CompanyDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create company request")
    public static class CreateRequest {

        @NotBlank(message = "Company name is required")
        @Size(max = 255)
        @Schema(description = "Company name", example = "Acme Corporation")
        private String name;

        @Size(max = 255)
        @Schema(description = "Legal name", example = "Acme Corp Inc.")
        private String legalName;

        @Size(max = 100)
        @Schema(description = "Registration number")
        private String registrationNumber;

        @Size(max = 100)
        @Schema(description = "Tax ID / GST / VAT number")
        private String taxId;

        @Email
        @Schema(description = "Business email", example = "contact@acme.com")
        private String email;

        @Size(max = 20)
        @Schema(description = "Business phone", example = "+1234567890")
        private String phone;

        @Schema(description = "Website URL")
        private String website;

        @Schema(description = "Address line 1")
        private String addressLine1;

        @Schema(description = "Address line 2")
        private String addressLine2;

        @Schema(description = "City")
        private String city;

        @Schema(description = "State/Province")
        private String state;

        @Schema(description = "Postal code")
        private String postalCode;

        @Schema(description = "Country")
        private String country;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update company request")
    public static class UpdateRequest {
        private String name;
        private String legalName;
        private String registrationNumber;
        private String taxId;
        private String email;
        private String phone;
        private String website;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String logoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update company settings")
    public static class SettingsRequest {

        @Size(min = 3, max = 3)
        @Schema(description = "Currency code (ISO 4217)", example = "USD")
        private String currency;

        @Schema(description = "Timezone", example = "America/New_York")
        private String timezone;

        @Schema(description = "Date format", example = "yyyy-MM-dd")
        private String dateFormat;

        @DecimalMin("0.00")
        @DecimalMax("100.00")
        @Schema(description = "Default tax rate %", example = "10.00")
        private BigDecimal defaultTaxRate;

        @Size(max = 10)
        @Schema(description = "Invoice number prefix", example = "INV")
        private String invoicePrefix;

        @Size(max = 1000)
        @Schema(description = "Invoice footer text")
        private String invoiceFooter;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Company response")
    public static class Response {
        private String id;
        private String tenantId;
        private String name;
        private String legalName;
        private String registrationNumber;
        private String taxId;
        private String email;
        private String phone;
        private String website;
        private String logoUrl;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String fullAddress;
        private String currency;
        private String timezone;
        private String dateFormat;
        private BigDecimal defaultTaxRate;
        private String invoicePrefix;
        private String invoiceFooter;
        private String subscriptionTier;
        private boolean active;
        private String createdAt;
        private String updatedAt;
    }
}
