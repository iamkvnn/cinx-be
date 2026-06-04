package com.cinx.learning.messaging.event;

import com.cinx.learning.dto.response.CourseResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseEvent {
    private CourseResponse course;
    private LocalDateTime timestamp;
}
