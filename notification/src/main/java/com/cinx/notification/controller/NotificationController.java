package com.cinx.notification.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.notification.service.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final INotificationService notificationService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<?>> getNotifications(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PaginationWrapper.wrap(notificationService.getNotifications(page, size)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnreadNotifications() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread notifications count retrieved successfully", notificationService.countUnreadNotifications()));
    }

    @PostMapping("/{notificationId}/toggle-read")
    public ResponseEntity<ApiResponse<Void>> toggleRead(@PathVariable String notificationId) {
        notificationService.toggleRead(notificationId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification read status toggled successfully", null));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification deleted successfully", null));
    }
}
