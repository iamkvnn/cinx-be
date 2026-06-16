package com.cinx.course.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseContentPublishedEvent {
    private String courseId;
    private String courseTitle;
    private String instructorId;

    public CourseContentPublishedEvent(String courseId, String courseTitle) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
    }
}
