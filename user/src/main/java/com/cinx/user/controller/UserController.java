package com.cinx.user.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.user.consts.Role;
import com.cinx.user.dto.UpdateProfileRequest;
import com.cinx.user.dto.UserDto;
import com.cinx.user.dto.request.DeviceTokenRequest;
import com.cinx.user.dto.request.TerminatePartnershipRequest;
import com.cinx.user.dto.request.UpdatePreferredCategoriesRequest;
import com.cinx.user.service.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @Operation(summary = "List all users (admin)", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedApiResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isInstructorVerified
    ) {
        return ResponseEntity.ok(
                PaginationWrapper.wrap(userService.findAll(page, size, query, role, isInstructorVerified, sort))
        );
    }

    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.name")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully", userService.findByUserId(id)));
    }

    @Operation(summary = "Approve instructor (admin)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/verify-instructor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> verifyInstructor(@PathVariable String id) {
        userService.verifyInstructor(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Instructor verified successfully", null));
    }

    @Operation(summary = "Reject instructor (admin)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/reject-instructor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> rejectInstructor(
            @PathVariable String id,
            @RequestParam String reason
    ) {
        userService.rejectInstructor(id, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "Instructor rejected successfully", null));
    }

    @Operation(summary = "Terminate partnership with instructor (admin)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/terminate-partnership")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> terminatePartnership(
            @PathVariable String id,
            @Valid @RequestBody TerminatePartnershipRequest request
    ) {
        userService.terminatePartnership(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Instructor partnership terminated successfully", null));
    }

    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Current user fetched successfully", userService.findByUserId(userId)));
    }

    @GetMapping("/ids")
    public ApiResponse<List<UserDto>> getUsersByIds(@RequestParam List<String> ids) {
        return new ApiResponse<>(true, "Users fetched successfully", userService.findByIds(ids));
    }

    @Operation(summary = "Update user profile", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.name")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateProfileRequest dto
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", userService.updateProfile(id, dto)));
    }

    @Operation(summary = "Save FCM device token", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/device-tokens")
    public ApiResponse<Void> saveDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        userService.saveDeviceToken(userId, request);
        return new ApiResponse<>(true, "Device token saved successfully", null);
    }

    @Operation(summary = "Update preferred categories", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/me/preferred-categories")
    public ApiResponse<Void> updatePreferredCategories(@Valid @RequestBody UpdatePreferredCategoriesRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        userService.updatePreferredCategories(userId, request.categoryIds());
        return new ApiResponse<>(true, "Preferred categories updated successfully", null);
    }
}
