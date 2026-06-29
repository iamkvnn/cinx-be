package com.cinx.notification.service.notification;

import com.cinx.notification.dto.request.CreateNotificationRequest;
import com.cinx.notification.mapper.UserNotificationMapper;
import com.cinx.notification.model.Notification;
import com.cinx.notification.model.UserNotification;
import com.cinx.notification.repository.NotificationRepository;
import com.cinx.notification.repository.UserNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private UserNotificationRepository userNotificationRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserNotificationMapper userNotificationMapper;

    @Test
    void getNotificationsDefaultsToNewestSentAtFirst() {
        NotificationService service = new NotificationService(
                userNotificationRepository, notificationRepository, userNotificationMapper);
        when(userNotificationRepository.findByUserId(eq(null), eq("user-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getNotifications("user-1", null, 1, 10, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userNotificationRepository).findByUserId(eq(null), eq("user-1"), pageableCaptor.capture());
        Sort.Order sentAtOrder = pageableCaptor.getValue().getSort().getOrderFor("sentAt");
        assertThat(sentAtOrder).isNotNull();
        assertThat(sentAtOrder.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendNotificationPersistsSentAtForEachRecipient() {
        NotificationService service = new NotificationService(
                userNotificationRepository, notificationRepository, userNotificationMapper);
        LocalDateTime sentAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        Notification notification = Notification.builder().id("notification-1").build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        service.sendNotification(new CreateNotificationRequest(
                "Title",
                "Message",
                "COURSE_QUESTION_CREATED",
                "question-1",
                "/courses/course-1/questions/question-1",
                Map.of("courseId", "course-1"),
                List.of("user-1", "user-2")
        ), sentAt);

        ArgumentCaptor<List<UserNotification>> captor = ArgumentCaptor.forClass(List.class);
        verify(userNotificationRepository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .hasSize(2)
                .allSatisfy(userNotification -> {
                    assertThat(userNotification.getNotificationId()).isEqualTo("notification-1");
                    assertThat(userNotification.getSentAt()).isEqualTo(sentAt);
                    assertThat(userNotification.getIsRead()).isFalse();
                });
    }
}
