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
 * User registration request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request")
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    @Schema(description = "Password (min 8 characters)", example = "SecureP@ss123")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Schema(description = "First name", example = "John")
    private String firstName;

    @Size(max = 100)
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Size(max = 20)
    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;

    @Size(max = 100)
    @Schema(description = "Company name - used to generate tenant ID for new companies", example = "Acme Corp")
    private String companyName;

    @Size(max = 50)
    @Schema(description = "Tenant/Company ID - optional, auto-generated if not provided", example = "acme-corp")
    private String tenantId;
}
