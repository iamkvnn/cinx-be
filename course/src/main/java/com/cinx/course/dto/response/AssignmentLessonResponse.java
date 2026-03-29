package com.cinx.course.dto.response;


import java.time.LocalDateTime;
import java.util.List;

public record AssignmentLessonResponse (
        String description,
        LocalDateTime startDate,
        LocalDateTime dueDate,
        List<AssignmentAttachmentResponse> attachments
){
}
