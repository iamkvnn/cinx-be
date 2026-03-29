package com.cinx.course.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAssignmentLessonRequest {
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime dueDate;
}
