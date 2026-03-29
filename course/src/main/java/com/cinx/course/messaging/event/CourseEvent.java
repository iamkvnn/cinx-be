package com.cinx.course.messaging.event;

import com.cinx.course.dto.response.CourseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CourseEvent {
    private CourseResponse course;
    private LocalDateTime timestamp;
}
