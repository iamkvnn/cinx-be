package com.cinx.notification.mapper;

import com.cinx.notification.model.Notification;
import com.cinx.notification.model.UserNotification;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserNotificationMapperTest {
    private final UserNotificationMapper mapper = Mappers.getMapper(UserNotificationMapper.class);

    @Test
    void toDtoIncludesSentAt() {
        LocalDateTime sentAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        Notification notification = Notification.builder()
                .title("Title")
                .message("Message")
                .type("COURSE_QUESTION_CREATED")
                .referenceId("question-1")
                .actionUrl("/courses/course-1/questions/question-1")
                .metadataJson("{}")
                .build();
        UserNotification userNotification = UserNotification.builder()
                .id("user-notification-1")
                .userId("user-1")
                .isRead(false)
                .sentAt(sentAt)
                .notification(notification)
                .build();

        var response = mapper.toDto(userNotification);

        assertThat(response.sentAt()).isEqualTo(sentAt);
    }
}
