package com.cinx.course.messaging.event;

import com.cinx.course.dto.response.CourseDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CourseEvent {
    private CourseDetailResponse course;
    private LocalDateTime timestamp;
}
