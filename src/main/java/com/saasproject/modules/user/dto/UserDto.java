package com.saasproject.modules.user.dto;

import com.saasproject.modules.auth.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * User management DTOs (separate from auth DTOs).
 */
public class UserDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create user request (admin)")
    public static class CreateRequest {

        @NotBlank(message = "Email is required")
        @Email
        @Schema(description = "User email", example = "user@example.com")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8)
        @Schema(description = "Password")
        private String password;

        @NotBlank(message = "First name is required")
        @Schema(description = "First name")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Schema(description = "Last name")
        private String lastName;

        @Schema(description = "Phone number")
        private String phone;

        @NotEmpty(message = "At least one role is required")
        @Schema(description = "User roles", example = "[\"CASHIER\"]")
        private Set<String> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update user request")
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String phone;
        private String avatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update user roles request")
    public static class RolesRequest {
        @NotEmpty(message = "At least one role is required")
        @Schema(description = "User roles", example = "[\"MANAGER\", \"CASHIER\"]")
        private Set<String> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User response")
    public static class Response {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phone;
        private String avatarUrl;
        private Set<String> roles;
        private boolean enabled;
        private boolean emailVerified;
        private String lastLoginAt;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User summary for lists")
    public static class Summary {
        private String id;
        private String email;
        private String fullName;
        private Set<String> roles;
        private boolean enabled;
    }
}
