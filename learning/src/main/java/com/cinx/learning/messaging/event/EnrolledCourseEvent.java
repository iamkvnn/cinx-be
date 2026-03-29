package com.cinx.learning.messaging.event;

import lombok.Data;

@Data
public class EnrolledCourseEvent {
    private String courseId;
    private String userId;
}
