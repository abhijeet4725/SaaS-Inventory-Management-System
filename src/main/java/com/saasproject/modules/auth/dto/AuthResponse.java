package com.saasproject.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Authentication response with tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response with tokens")
public class AuthResponse {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "JWT refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiry in seconds", example = "900")
    private long expiresIn;

    @Schema(description = "User information")
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User information")
    public static class UserInfo {

        @Schema(description = "User ID")
        private String id;

        @Schema(description = "User email")
        private String email;

        @Schema(description = "First name")
        private String firstName;

        @Schema(description = "Last name")
        private String lastName;

        @Schema(description = "Full name")
        private String fullName;

        @Schema(description = "Tenant ID")
        private String tenantId;

        @Schema(description = "User roles")
        private Set<String> roles;
    }
}
