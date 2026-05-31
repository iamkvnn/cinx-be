package com.cinx.course.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonChangedEvent {
    private String courseId;
    private String lessonId;
    private String changeType;
    /** Embedded to avoid Feign call in notification service. */
    private String courseTitle;
}

