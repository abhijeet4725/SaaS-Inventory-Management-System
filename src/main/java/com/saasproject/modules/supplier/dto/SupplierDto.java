package com.saasproject.modules.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Supplier DTOs.
 */
public class SupplierDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create supplier request")
    public static class CreateRequest {

        @Size(max = 50)
        @Schema(description = "Supplier code", example = "SUP-001")
        private String supplierCode;

        @NotBlank(message = "Name is required")
        @Size(max = 255)
        @Schema(description = "Supplier name", example = "ABC Supplies")
        private String name;

        @Size(max = 255)
        @Schema(description = "Contact person name")
        private String contactPerson;

        @Email
        @Schema(description = "Email address")
        private String email;

        @Size(max = 20)
        @Schema(description = "Phone number")
        private String phone;

        @Schema(description = "Website URL")
        private String website;

        @Schema(description = "Address line 1")
        private String addressLine1;

        @Schema(description = "City")
        private String city;

        @Schema(description = "State/Province")
        private String state;

        @Schema(description = "Postal code")
        private String postalCode;

        @Schema(description = "Country")
        private String country;

        @Size(max = 100)
        @Schema(description = "Tax ID")
        private String taxId;

        @Schema(description = "Payment terms", example = "Net 30")
        private String paymentTerms;

        @DecimalMin("0.00")
        @Schema(description = "Credit limit")
        private BigDecimal creditLimit;

        @Schema(description = "Notes")
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update supplier request")
    public static class UpdateRequest {
        private String supplierCode;
        private String name;
        private String contactPerson;
        private String email;
        private String phone;
        private String altPhone;
        private String website;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String taxId;
        private String paymentTerms;
        private BigDecimal creditLimit;
        private String bankName;
        private String bankAccount;
        private String notes;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Supplier response")
    public static class Response {
        private String id;
        private String supplierCode;
        private String name;
        private String contactPerson;
        private String email;
        private String phone;
        private String altPhone;
        private String website;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String fullAddress;
        private String taxId;
        private String paymentTerms;
        private BigDecimal creditLimit;
        private BigDecimal outstandingBalance;
        private String bankName;
        private String notes;
        private boolean active;
        private String createdAt;
        private String updatedAt;
    }
}
