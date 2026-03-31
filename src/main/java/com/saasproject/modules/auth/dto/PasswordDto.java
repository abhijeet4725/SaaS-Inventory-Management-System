package com.saasproject.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Password reset DTOs.
 */
public class PasswordDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Forgot password request")
    public static class ForgotPasswordRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "User email", example = "user@example.com")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Reset password request")
    public static class ResetPasswordRequest {

        @NotBlank(message = "Token is required")
        @Schema(description = "Password reset token")
        private String token;

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
        @Schema(description = "New password", example = "NewSecureP@ss123")
        private String newPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Change password request")
    public static class ChangePasswordRequest {

        @NotBlank(message = "Current password is required")
        @Schema(description = "Current password")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
        @Schema(description = "New password", example = "NewSecureP@ss123")
        private String newPassword;
    }
}
