package com.saasproject.modules.user.service;

import com.saasproject.common.exceptions.BusinessException;
import com.saasproject.common.exceptions.ResourceNotFoundException;
import com.saasproject.common.utils.CommonUtils;
import com.saasproject.modules.auth.entity.Role;
import com.saasproject.modules.auth.entity.User;
import com.saasproject.modules.auth.repository.UserRepository;
import com.saasproject.modules.user.dto.UserDto;
import com.saasproject.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User management service (admin operations).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user (admin action).
     */
    @Transactional
    public UserDto.Response createUser(UserDto.CreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating user: {} for tenant: {}", request.getEmail(), tenantId);

        // Check if email exists
        if (userRepository.existsByEmailAndTenantIdAndDeletedFalse(request.getEmail(), tenantId)) {
            throw new BusinessException("EMAIL_EXISTS", "Email already registered");
        }

        Set<Role> roles = request.getRoles().stream()
                .map(r -> Role.valueOf(r.toUpperCase()))
                .collect(Collectors.toSet());

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .roles(roles)
                .enabled(true)
                .emailVerified(true) // Admin-created users are pre-verified
                .build();

        user.setTenantId(tenantId);
        user = userRepository.save(user);

        log.info("User created by admin: {}", user.getId());
        return mapToResponse(user);
    }

    /**
     * Get user by ID.
     */
    @Transactional(readOnly = true)
    public UserDto.Response getUser(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();

        User user = userRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        return mapToResponse(user);
    }

    /**
     * Get all users for tenant.
     */
    @Transactional(readOnly = true)
    public Page<UserDto.Response> getUsers(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return userRepository.findByTenant(tenantId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Update user details.
     */
    @Transactional
    public UserDto.Response updateUser(UUID id, UserDto.UpdateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating user: {}", id);

        User user = userRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null)
            user.setAvatarUrl(request.getAvatarUrl());

        user = userRepository.save(user);
        log.info("User updated: {}", user.getId());

        return mapToResponse(user);
    }

    /**
     * Update user roles.
     */
    @Transactional
    public UserDto.Response updateRoles(UUID id, UserDto.RolesRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating roles for user: {}", id);

        User user = userRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        Set<Role> roles = request.getRoles().stream()
                .map(r -> Role.valueOf(r.toUpperCase()))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        user = userRepository.save(user);

        log.info("Roles updated for user: {}", user.getId());
        return mapToResponse(user);
    }

    /**
     * Enable/disable user.
     */
    @Transactional
    public UserDto.Response setEnabled(UUID id, boolean enabled) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Setting enabled={} for user: {}", enabled, id);

        User user = userRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        user.setEnabled(enabled);
        user = userRepository.save(user);

        log.info("User {} {}", user.getId(), enabled ? "enabled" : "disabled");
        return mapToResponse(user);
    }

    /**
     * Delete user (soft delete).
     */
    @Transactional
    public void deleteUser(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting user: {}", id);

        User user = userRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        user.softDelete();
        userRepository.save(user);

        log.info("User soft deleted: {}", id);
    }

    /**
     * Reset user password (admin action).
     */
    @Transactional
    public void resetPassword(UUID id, String newPassword) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Resetting password for user: {}", id);

        User user = userRepository.findByIdAndTenant(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset for user: {}", id);
    }

    private UserDto.Response mapToResponse(User user) {
        return UserDto.Response.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(CommonUtils.formatDateTime(user.getLastLoginAt()))
                .createdAt(CommonUtils.formatDateTime(user.getCreatedAt()))
                .build();
    }
}
