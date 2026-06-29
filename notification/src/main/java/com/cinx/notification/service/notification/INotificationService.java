package com.cinx.notification.service.notification;

import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.dto.response.UserNotificationResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface INotificationService {
    Page<UserNotificationResponse> getNotifications(String userId, String query, int page, int size, String sort);
    Long countUnreadNotifications(String userId);
    void sendNotification(CreateNotificationRequest request);
    void sendNotification(CreateNotificationRequest request, LocalDateTime sentAt);
    void toggleRead(String userId, String notificationId);
    void markAllAsRead(String userId);
    void markAllAsUnread(String userId);
    void deleteNotification(String userId, String notificationId);
}
