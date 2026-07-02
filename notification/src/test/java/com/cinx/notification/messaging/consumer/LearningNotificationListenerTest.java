package com.cinx.notification.messaging.consumer;

import com.cinx.notification.messaging.context.NotificationContext;
import com.cinx.notification.messaging.event.learning.CourseCompletedEvent;
import com.cinx.notification.service.dispatch.INotificationDispatchService;
import com.cinx.notification.service.format.NotificationFormatter;
import com.cinx.notification.service.idempotency.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LearningNotificationListenerTest {
    @Mock
    private INotificationDispatchService dispatchService;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private Channel channel;

    private LearningNotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new LearningNotificationListener(
                dispatchService,
                idempotencyService,
                new NotificationFormatter(),
                new ObjectMapper()
        );
    }

    @Test
    void courseCompletedPayloadNotifiesLearner() throws Exception {
        CourseCompletedEvent event = CourseCompletedEvent.builder()
                .userId("user-1")
                .courseId("course-1")
                .courseTitle("Course title")
                .build();

        listener.handleCourseCompleted(event, channel, 10L, "message-1");

        ArgumentCaptor<NotificationContext> captor = ArgumentCaptor.forClass(NotificationContext.class);
        verify(dispatchService).dispatch(captor.capture());
        assertThat(captor.getValue().channels()).containsExactly("IN_APP", "PUSH");
        assertThat(userIds(captor.getValue())).containsExactly("user-1");
        verify(idempotencyService).markSuccess("message-1");
        verify(channel).basicAck(10L, false);
    }

    @Test
    void genericCourseCompletedPayloadDispatchesByEventTypeHeader() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", "user-1");
        payload.put("courseId", "course-1");
        payload.put("courseTitle", "Course title");

        listener.handleGenericLearningEvent(payload, channel, 11L, "message-2", "learning.course.completed");

        ArgumentCaptor<NotificationContext> captor = ArgumentCaptor.forClass(NotificationContext.class);
        verify(dispatchService).dispatch(captor.capture());
        assertThat(captor.getValue().channels()).containsExactly("IN_APP", "PUSH");
        assertThat(userIds(captor.getValue())).containsExactly("user-1");
        verify(idempotencyService).markSuccess("message-2");
        verify(channel).basicAck(11L, false);
    }

    @Test
    void genericUnsupportedPayloadIsAckedWithoutRetry() throws Exception {
        listener.handleGenericLearningEvent(Map.of("eventId", "event-1"), channel, 12L, "message-3", "UnknownEvent");

        verify(dispatchService, never()).dispatch(org.mockito.ArgumentMatchers.any());
        verify(channel).basicAck(12L, false);
    }

    @SuppressWarnings("unchecked")
    private List<String> userIds(NotificationContext context) {
        return (List<String>) context.inAppPayload().get("userIds");
    }
}
