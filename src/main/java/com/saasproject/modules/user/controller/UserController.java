package com.saasproject.modules.user.controller;

import com.saasproject.common.api_response.ApiResponse;
import com.saasproject.common.dto.PageRequestDto;
import com.saasproject.modules.user.dto.UserDto;
import com.saasproject.modules.user.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * User management controller for admin operations.
 */
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management", description = "Admin user management")
public class UserController {

    private final UserManagementService userService;

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user (admin only)")
    public ResponseEntity<ApiResponse<UserDto.Response>> createUser(
            @Valid @RequestBody UserDto.CreateRequest request) {

        UserDto.Response response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @GetMapping
    @Operation(summary = "List users", description = "Get all users for the tenant")
    public ResponseEntity<ApiResponse<List<UserDto.Response>>> getUsers(
            @ModelAttribute PageRequestDto pageRequest) {

        Page<UserDto.Response> page = userService.getUsers(pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.paginated(
                page.getContent(),
                ApiResponse.PageInfo.of(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user", description = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDto.Response>> getUser(
            @PathVariable UUID id) {

        UserDto.Response response = userService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user details")
    public ResponseEntity<ApiResponse<UserDto.Response>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserDto.UpdateRequest request) {

        UserDto.Response response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated", response));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update roles", description = "Update user roles")
    public ResponseEntity<ApiResponse<UserDto.Response>> updateRoles(
            @PathVariable UUID id,
            @Valid @RequestBody UserDto.RolesRequest request) {

        UserDto.Response response = userService.updateRoles(id, request);
        return ResponseEntity.ok(ApiResponse.success("Roles updated", response));
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Enable user", description = "Enable a disabled user")
    public ResponseEntity<ApiResponse<UserDto.Response>> enableUser(@PathVariable UUID id) {

        UserDto.Response response = userService.setEnabled(id, true);
        return ResponseEntity.ok(ApiResponse.success("User enabled", response));
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "Disable user", description = "Disable a user")
    public ResponseEntity<ApiResponse<UserDto.Response>> disableUser(@PathVariable UUID id) {

        UserDto.Response response = userService.setEnabled(id, false);
        return ResponseEntity.ok(ApiResponse.success("User disabled", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Soft delete a user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {

        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password (admin only)")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable UUID id,
            @RequestBody java.util.Map<String, String> body) {

        userService.resetPassword(id, body.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success("Password reset"));
    }
}
