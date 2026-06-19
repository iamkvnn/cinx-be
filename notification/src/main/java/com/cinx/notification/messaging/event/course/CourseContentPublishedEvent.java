package com.cinx.notification.messaging.event.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseContentPublishedEvent {
    private String courseId;
    private String courseTitle;
    private String instructorId;

    public CourseContentPublishedEvent(String courseId, String courseTitle) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
    }
}
