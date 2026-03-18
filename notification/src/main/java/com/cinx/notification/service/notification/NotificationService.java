package com.cinx.notification.service.notification;

import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.dto.response.UserNotificationResponse;
import com.cinx.notification.repository.NotificationRepository;
import com.cinx.notification.mapper.UserNotificationMapper;
import com.cinx.notification.model.Notification;
import com.cinx.notification.model.UserNotification;
import com.cinx.notification.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService{
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationMapper userNotificationMapper;

    @Override
    public Page<UserNotificationResponse> getNotifications(int page, int size) {
        String userId = AuthenticationUtil.extractUserId();
        return userNotificationRepository.findByUserId(userId, PageRequest.of(page - 1, size))
                .map(userNotificationMapper::toDto);
    }

    @Override
    public Long countUnreadNotifications() {
        String userId = AuthenticationUtil.extractUserId();
        return userNotificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void sendNotification(CreateNotificationRequest request) {
        Notification notification = notificationRepository.save(Notification.builder()
                    .title(request.title())
                    .message(request.message())
                    .build());
        userNotificationRepository.saveAll(request.userIds().stream()
                .map(userId -> UserNotification.builder()
                        .userId(userId)
                        .notification(notification)
                        .isRead(false)
                        .build())
                .toList());
    }

    @Override
    public void toggleRead(String notificationId) {
        userNotificationRepository.findById(notificationId).ifPresent(userNotification -> {
            userNotification.setIsRead(!userNotification.getIsRead());
            userNotificationRepository.save(userNotification);
        });
    }

    @Transactional
    @Override
    public void deleteNotification(String notificationId) {
        userNotificationRepository.deleteById(notificationId);
    }
}
