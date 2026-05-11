package com.cinx.course.dto.response;

import java.util.List;

public record AssignmentLessonResponse (
        String description,
        List<AssignmentAttachmentResponse> attachments
){
}
