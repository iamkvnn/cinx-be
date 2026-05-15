package com.cinx.notification.messaging.event.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonChangedEvent {
    private String courseId;
    private String lessonId;
    private String changeType;
}