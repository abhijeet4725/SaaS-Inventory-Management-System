package com.saasproject.config;

import com.saasproject.modules.auth.security.JwtAuthenticationEntryPoint;
import com.saasproject.modules.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for JWT-based stateless authentication.
 * 
 * Features:
 * - Stateless session management
 * - JWT token validation filter
 * - BCrypt password encoding
 * - Role-based method security
 * - CSRF disabled for API
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
        private final UserDetailsService userDetailsService;

        // Public endpoints that don't require authentication
        private static final String[] PUBLIC_ENDPOINTS = {
                        // Auth endpoints
                        "/v1/auth/**",
                        "/api/v1/auth/**",
                        // Swagger/OpenAPI
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/docs/**",
                        "/v3/api-docs/**",
                        "/api/v3/api-docs/**",
                        // Actuator
                        "/actuator/**",
                        "/actuator/health",
                        "/actuator/info",
                        // WebSocket
                        "/ws/**"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                // Disable CSRF for stateless API
                                .csrf(AbstractHttpConfigurer::disable)

                                // Configure CORS (handled by CorsConfig)
                                .cors(cors -> {
                                })

                                // Stateless session management for JWT
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Exception handling
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // Admin-only endpoints
                                                .requestMatchers("/v1/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/v1/users/**").hasAnyRole("ADMIN", "MANAGER")

                                                // Manager+ endpoints
                                                .requestMatchers(HttpMethod.DELETE, "/v1/**")
                                                .hasAnyRole("ADMIN", "MANAGER")

                                                // All other endpoints require authentication
                                                .anyRequest().authenticated())

                                // Add JWT authentication filter
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                                // Security headers
                                .headers(headers -> headers
                                                .contentSecurityPolicy(
                                                                csp -> csp.policyDirectives(
                                                                                "default-src 'self'; frame-ancestors 'self'"))
                                                .frameOptions(frame -> frame.sameOrigin()))

                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
                        throws Exception {
                return config.getAuthenticationManager();
        }
}
