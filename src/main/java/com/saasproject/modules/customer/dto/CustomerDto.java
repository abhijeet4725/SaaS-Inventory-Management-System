 package com.saasproject.modules.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Customer DTOs.
 */
public class CustomerDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create customer request")
    public static class CreateRequest {

        @NotBlank(message = "Name is required")
        @Size(max = 255)
        @Schema(description = "Customer name", example = "John Doe")
        private String name;

        @Email
        @Schema(description = "Email address")
        private String email;

        @Size(max = 20)
        @Schema(description = "Phone number")
        private String phone;

        @Schema(description = "Customer type", example = "INDIVIDUAL")
        private String customerType;

        @Schema(description = "Company name (for business)")
        private String companyName;

        @Schema(description = "Tax ID")
        private String taxId;

        @Schema(description = "Address line 1")
        private String addressLine1;

        @Schema(description = "City")
        private String city;

        @Schema(description = "State")
        private String state;

        @Schema(description = "Postal code")
        private String postalCode;

        @Schema(description = "Country")
        private String country;

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
    @Schema(description = "Update customer request")
    public static class UpdateRequest {
        private String customerCode;
        private String name;
        private String email;
        private String phone;
        private String altPhone;
        private String customerType;
        private String companyName;
        private String taxId;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private BigDecimal creditLimit;
        private String notes;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Customer response")
    public static class Response {
        private String id;
        private String customerCode;
        private String name;
        private String email;
        private String phone;
        private String altPhone;
        private String customerType;
        private String companyName;
        private String taxId;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String fullAddress;
        private BigDecimal creditLimit;
        private BigDecimal outstandingBalance;
        private BigDecimal totalPurchases;
        private int loyaltyPoints;
        private String notes;
        private boolean active;
        private String createdAt;
    }
}
