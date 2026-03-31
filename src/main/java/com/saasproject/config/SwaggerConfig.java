package com.saasproject.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger configuration.
 * 
 * Access documentation at:
 * - Swagger UI: /api/swagger-ui.html
 * - OpenAPI JSON: /api/docs
 * 
 * All endpoints require JWT authentication unless
 * marked @SecurityRequirements(value = {})
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "SaaS Inventory + Billing + POS API", version = "1.0.0", description = """
        Production-ready REST API for SaaS-based Inventory Management, Billing, and Point of Sale.

        ## Features
        - Multi-tenant architecture
        - JWT authentication with role-based access
        - Real-time WebSocket updates
        - Comprehensive audit logging

        ## Authentication
        Use the `POST /v1/auth/login` endpoint to obtain a JWT token, then use the **Authorize** button
        above to add it to your requests.

        ## Rate Limiting
        API requests are rate-limited to 100 requests/minute per user.
        """, contact = @Contact(name = "API Support", email = "support@example.com"), license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")), servers = {
        @Server(url = "/", description = "Current Server"),
        @Server(url = "http://localhost:8080/api", description = "Local Development"),
        @Server(url = "https://staging.example.com/api", description = "Staging"),
        @Server(url = "https://api.example.com", description = "Production")
}, security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", description = "JWT authentication. Obtain token from /v1/auth/login", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class SwaggerConfig {
    // Configuration is done via annotations
    // Swagger UI available at: /api/swagger-ui.html
    // API docs available at: /api/docs
}
