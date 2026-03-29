package com.cinx.notification.service.notification;

import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.dto.response.UserNotificationResponse;
import org.springframework.data.domain.Page;

public interface INotificationService {
    Page<UserNotificationResponse> getNotifications(int page, int size);
    Long countUnreadNotifications();
    void sendNotification(CreateNotificationRequest request);
    void toggleRead(String notificationId);
    void deleteNotification(String notificationId);
}
