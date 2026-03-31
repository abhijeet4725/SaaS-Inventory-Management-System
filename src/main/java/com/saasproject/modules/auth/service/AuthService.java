package com.saasproject.modules.auth.service;

import com.saasproject.common.exceptions.BusinessException;
import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.auth.dto.*;
import com.saasproject.modules.auth.entity.RefreshToken;
import com.saasproject.modules.auth.entity.Role;
import com.saasproject.modules.auth.entity.User;
import com.saasproject.modules.auth.repository.RefreshTokenRepository;
import com.saasproject.modules.auth.repository.UserRepository;
import com.saasproject.modules.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication service handling login, registration, and token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Auto-generate tenantId if not provided
        String tenantId = request.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
                // Generate tenantId from company name (lowercase, replace spaces with dashes)
                tenantId = request.getCompanyName()
                        .toLowerCase()
                        .replaceAll("[^a-z0-9\\s-]", "")
                        .replaceAll("\\s+", "-")
                        .replaceAll("-+", "-")
                        .replaceAll("^-|-$", "");
                // Add a short unique suffix to avoid collisions
                tenantId = tenantId + "-" + CommonUtils.generateShortId().substring(0, 6);
            } else {
                // Generate a random tenantId
                tenantId = "tenant-" + CommonUtils.generateShortId();
            }
        }

        // Check if email already exists in this tenant
        if (userRepository.existsByEmailAndTenantIdAndDeletedFalse(
                request.getEmail(), tenantId)) {
            throw new BusinessException("EMAIL_EXISTS", "Email already registered");
        }

        // First user in tenant gets ADMIN role, others get CASHIER
        boolean isFirstUserInTenant = !userRepository.existsByTenantIdAndDeletedFalse(tenantId);
        Set<Role> roles = isFirstUserInTenant ? Set.of(Role.ADMIN) : Set.of(Role.CASHIER);

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(roles)
                .enabled(true)
                .emailVerified(false)
                .build();
        user.setTenantId(tenantId);

        user = userRepository.save(user);
        log.info("User registered successfully: {} with tenantId: {}", user.getId(), tenantId);

        return generateAuthResponse(user);
    }

    /**
     * Authenticate user and return tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            User user = (User) authentication.getPrincipal();

            // Record successful login
            user.recordLoginSuccess(CommonUtils.getClientIpAddress());
            userRepository.save(user);

            log.info("Login successful for user: {}", user.getId());
            return generateAuthResponse(user);

        } catch (BadCredentialsException e) {
            // Record failed login attempt
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(user -> {
                        user.recordLoginFailure();
                        userRepository.save(user);
                    });

            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Refresh access token using refresh token.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Refreshing token");

        RefreshToken refreshToken = refreshTokenRepository
                .findValidToken(request.getRefreshToken(), LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Invalid or expired refresh token"));

        User user = refreshToken.getUser();

        // Revoke old refresh token
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info("Token refreshed for user: {}", user.getId());
        return generateAuthResponse(user);
    }

    /**
     * Logout user by revoking all refresh tokens.
     */
    @Transactional
    public void logout(String email) {
        log.info("Logging out user: {}", email);

        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
                    log.info("All tokens revoked for user: {}", user.getId());
                });
    }

    /**
     * Request password reset (sends email with reset token).
     */
    @Transactional
    public void forgotPassword(PasswordDto.ForgotPasswordRequest request) {
        log.info("Password reset requested for: {}", request.getEmail());

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    // TODO: Generate reset token and send email
                    // For now, just log it
                    String resetToken = CommonUtils.generateShortId();
                    log.info("Password reset token for {}: {}", user.getEmail(), resetToken);
                });

        // Always return success to prevent email enumeration
    }

    /**
     * Reset password using reset token.
     */
    @Transactional
    public void resetPassword(PasswordDto.ResetPasswordRequest request) {
        // TODO: Validate reset token and update password
        log.info("Password reset completed");
        throw new BusinessException("NOT_IMPLEMENTED", "Password reset not yet implemented");
    }

    /**
     * Change password for authenticated user.
     */
    @Transactional
    public void changePassword(String email, PasswordDto.ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_PASSWORD", "Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Revoke all refresh tokens (force re-login on all devices)
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());

        log.info("Password changed for user: {}", user.getId());
    }

    // ===== Private Methods =====

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user, user.getTenantId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, user.getTenantId());

        // Save refresh token
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .tenantId(user.getTenantId())
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtTokenProvider.getRefreshTokenExpiration() / 1000))
                .ipAddress(CommonUtils.getClientIpAddress())
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .fullName(user.getFullName())
                        .tenantId(user.getTenantId())
                        .roles(user.getRoles().stream()
                                .map(Enum::name)
                                .collect(Collectors.toSet()))
                        .build())
                .build();
    }
}
