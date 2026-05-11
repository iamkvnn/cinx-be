package com.cinx.course.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LessonChangedEvent {
    private String courseId;
    private String lessonId;
    private String changeType;
}

