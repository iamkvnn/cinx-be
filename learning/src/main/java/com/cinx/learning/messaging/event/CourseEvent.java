package com.cinx.learning.messaging.event;

import com.cinx.learning.dto.response.CourseDetailResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseEvent {
    private CourseDetailResponse course;
    private LocalDateTime timestamp;
}
