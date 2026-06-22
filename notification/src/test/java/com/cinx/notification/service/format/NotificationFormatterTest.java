package com.cinx.notification.service.format;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationFormatterTest {

    private final NotificationFormatter formatter = new NotificationFormatter();

    @Test
    void coursePublishedForLearnersBuildsVietnamesePayloadWithAction() {
        NotificationMessage message = formatter.coursePublishedForLearners("course-1", "Java <Basic>");

        assertThat(message.type()).isEqualTo("COURSE_CONTENT_PUBLISHED");
        assertThat(message.title()).isEqualTo("Nội dung khóa học mới");
        assertThat(message.message()).contains("\"Java <Basic>\"");
        assertThat(message.actionUrl()).isEqualTo("/courses/course-1");
        assertThat(message.metadata()).containsEntry("courseId", "course-1");
        assertThat(message.htmlBody()).contains("Java &lt;Basic&gt;");
        assertThat(message.inAppPayload(List.of("user-1")))
                .containsEntry("type", "COURSE_CONTENT_PUBLISHED")
                .containsEntry("referenceId", "course-1")
                .containsEntry("actionUrl", "/courses/course-1");
    }

    @Test
    void dailyReminderBuildsPushData() {
        NotificationMessage message = formatter.dailyReminder("XP", 50, 10, "lesson-1");

        assertThat(message.title()).isEqualTo("Đừng để mất nhịp học");
        assertThat(message.message()).contains("50 XP");
        assertThat(message.pushPayload(List.of("user-1")).get("data"))
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("type", "DAILY_LEARNING_REMINDER")
                .containsEntry("targetItemId", "lesson-1");
    }
}
