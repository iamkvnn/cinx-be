package com.cinx.user.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.user.consts.Role;
import com.cinx.user.dto.UpdateProfileRequest;
import com.cinx.user.dto.UserDto;
import com.cinx.user.dto.request.DeviceTokenRequest;
import com.cinx.user.service.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    // ─── Admin endpoints ───────────────────────────────────────────────────────

    /** List all users — admin only */
    @Operation(summary = "List all users (admin)", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
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

    /** Get any user by ID — admin only */
    @Operation(summary = "Get user by ID (admin)", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully", userService.findByUserId(id)));
    }

    /** Approve instructor verification — admin only */
    @Operation(summary = "Approve instructor (admin)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/verify-instructor")
    public ResponseEntity<ApiResponse<?>> verifyInstructor(@PathVariable String id) {
        userService.verifyInstructor(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Instructor verified successfully", null));
    }

    /** Reject instructor verification — admin only */
    @Operation(summary = "Reject instructor (admin)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/reject-instructor")
    public ResponseEntity<ApiResponse<?>> rejectInstructor(
            @PathVariable String id,
            @RequestParam String reason
    ) {
        userService.rejectInstructor(id, reason);
        return ResponseEntity.ok(new ApiResponse<>(true, "Instructor rejected successfully", null));
    }

    // ─── Authenticated user endpoints ──────────────────────────────────────────

    /** Get the currently authenticated user's profile */
    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Current user fetched successfully", userService.findByUserId(userId)));
    }

    /** Update any user's profile (own or admin) */
    @Operation(summary = "Update user profile", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateProfileRequest dto
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", userService.updateProfile(id, dto)));
    }

    /** Save / register an FCM device token for push notifications */
    @Operation(summary = "Save FCM device token", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/device-tokens")
    public ApiResponse<Void> saveDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        userService.saveDeviceToken(userId, request);
        return new ApiResponse<>(true, "Device token saved successfully", null);
    }
}
