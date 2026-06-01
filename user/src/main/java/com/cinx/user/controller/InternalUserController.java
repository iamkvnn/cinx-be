package com.cinx.user.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.user.dto.CreateUserRequest;
import com.cinx.user.dto.UserDto;
import com.cinx.user.service.user.IUserService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Internal API — called only by other services via Feign (service-to-service).
 * Not exposed externally; blocked at the gateway layer (/internal/** → denyAll).
 */
@Hidden
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final IUserService userService;

    @PostMapping
    public ApiResponse<UserDto> createUser(@RequestBody CreateUserRequest request) {
        return new ApiResponse<>(true, "User created successfully", userService.createUser(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUserById(@PathVariable String id) {
        return new ApiResponse<>(true, "User fetched successfully", userService.findByUserId(id));
    }

    @GetMapping("/ids")
    public ApiResponse<List<UserDto>> getUsersByIds(@RequestParam List<String> ids) {
        return new ApiResponse<>(true, "Users fetched successfully", userService.findByIds(ids));
    }

    @GetMapping("/{userId}/instructor-verified")
    public ApiResponse<Boolean> checkInstructorVerified(@PathVariable String userId) {
        return new ApiResponse<>(true, "Instructor verification status fetched successfully",
                userService.checkInstructorVerified(userId));
    }

    @PostMapping("/{userId}/toggle-ban")
    public ApiResponse<Void> toggleBanUser(@PathVariable String userId) {
        userService.toggleBan(userId);
        return new ApiResponse<>(true, "User ban status toggled successfully", null);
    }

    @GetMapping("/{userId}/fcm-tokens")
    public ApiResponse<List<String>> getUserFcmTokens(@PathVariable String userId) {
        return new ApiResponse<>(true, "FCM tokens fetched successfully", userService.getUserTokens(userId));
    }

    @PostMapping("/{userId}/add-xp")
    public ApiResponse<UserDto> addXp(@PathVariable String userId, @RequestParam Integer amount) {
        return new ApiResponse<>(true, "XP added successfully", userService.addXp(userId, amount));
    }

    @PostMapping("/{userId}/last-access")
    public ApiResponse<Void> updateLastAccess(@PathVariable String userId) {
        userService.updateLastAccess(userId);
        return new ApiResponse<>(true, "Last access updated successfully", null);
    }

    @GetMapping("/metrics/total-count")
    public ApiResponse<Long> getTotalUsersCount() {
        return new ApiResponse<>(true, "Total users count fetched successfully", userService.countTotalUsers());
    }

    @GetMapping("/metrics/new-count")
    public ApiResponse<Long> getNewUsersCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return new ApiResponse<>(true, "New users count fetched successfully", userService.countUsersBetween(start, end));
    }
}
