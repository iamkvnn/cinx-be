package com.cinx.notification.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.notification.dto.response.UserNotificationResponse;
import com.cinx.notification.service.notification.INotificationService;
import com.cinx.notification.service.push.PushNotificationService;
import com.cinx.notification.strategy.InAppNotificationStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cinx.common.utils.AuthenticationUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final INotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    private final InAppNotificationStrategy inAppNotificationStrategy;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<PaginatedApiResponse<UserNotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(PaginationWrapper.wrap(notificationService.getNotifications(userId, query, page, size, sort)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnreadNotifications() {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread notifications count retrieved successfully", notificationService.countUnreadNotifications(userId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{notificationId}/toggle-read")
    public ResponseEntity<ApiResponse<Void>> toggleRead(@PathVariable String notificationId) {
        String userId = AuthenticationUtil.extractUserId();
        notificationService.toggleRead(userId, notificationId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification read status toggled successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String notificationId) {
        String userId = AuthenticationUtil.extractUserId();
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification deleted successfully", null));
    }

    @Operation(summary = "Test Push Notification", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/test-push")
    public ResponseEntity<ApiResponse<Void>> testPushNotification(@RequestParam String title, @RequestParam String body) {
        String userId = AuthenticationUtil.extractUserId();
        pushNotificationService.sendPushNotificationToUser(userId, title, body);
        return ResponseEntity.ok(new ApiResponse<>(true, "Push notification sent", null));
    }

    @PostMapping("/test-in-app")
    public ResponseEntity<ApiResponse<Void>> testInAppNotification(@RequestParam String userId, @RequestParam String title, @RequestParam String body) {
        inAppNotificationStrategy.send(Map.of(
                "userIds", List.of(userId),
                "title", title,
                "message", body
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "In-app notification sent", null));
    }
}
