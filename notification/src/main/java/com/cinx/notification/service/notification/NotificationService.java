package com.cinx.notification.service.notification;

import com.cinx.common.mapper.SortConverter;
import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.dto.response.UserNotificationResponse;
import com.cinx.notification.repository.NotificationRepository;
import com.cinx.notification.mapper.UserNotificationMapper;
import com.cinx.notification.model.Notification;
import com.cinx.notification.model.UserNotification;
import com.cinx.notification.repository.UserNotificationRepository;
import com.cinx.notification.utils.NotificationJson;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService{
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationMapper userNotificationMapper;

    @Override
    public Page<UserNotificationResponse> getNotifications(String userId, String query, int page, int size, String sort) {
        Sort resolvedSort = sort == null || sort.isBlank()
                ? Sort.by(Sort.Direction.DESC, "sentAt")
                : SortConverter.toSort(sort);
        return userNotificationRepository.findByUserId(query, userId, PageRequest.of(page - 1, size, resolvedSort))
                .map(userNotificationMapper::toDto);
    }

    @Override
    public Long countUnreadNotifications(String userId) {
        return userNotificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public void sendNotification(CreateNotificationRequest request) {
        sendNotification(request, LocalDateTime.now());
    }

    @Override
    public void sendNotification(CreateNotificationRequest request, LocalDateTime sentAt) {
        Notification notification = notificationRepository.save(Notification.builder()
                    .title(request.title())
                    .message(request.message())
                    .type(request.type())
                    .referenceId(request.referenceId())
                    .actionUrl(request.actionUrl())
                    .metadataJson(NotificationJson.write(request.metadata()))
                    .build());
        userNotificationRepository.saveAll(request.userIds().stream()
                .map(userId -> UserNotification.builder()
                        .userId(userId)
                        .notificationId(notification.getId())
                        .notification(notification)
                        .isRead(false)
                        .sentAt(sentAt)
                        .build())
                .toList());
    }

    @Override
    public void toggleRead(String userId, String notificationId) {
        userNotificationRepository.findById(notificationId).ifPresent(userNotification -> {
            userNotification.setIsRead(!userNotification.getIsRead());
            userNotificationRepository.save(userNotification);
        });
    }

    @Transactional
    @Override
    public void markAllAsRead(String userId) {
        userNotificationRepository.updateReadStatusByUserId(userId, true);
    }

    @Transactional
    @Override
    public void markAllAsUnread(String userId) {
        userNotificationRepository.updateReadStatusByUserId(userId, false);
    }

    @Transactional
    @Override
    public void deleteNotification(String userId, String notificationId) {
        userNotificationRepository.deleteById(notificationId);
    }
}
