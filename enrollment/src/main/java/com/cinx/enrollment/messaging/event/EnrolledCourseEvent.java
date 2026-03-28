package com.cinx.enrollment.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnrolledCourseEvent {
    private String courseId;
    private String userId;
}
