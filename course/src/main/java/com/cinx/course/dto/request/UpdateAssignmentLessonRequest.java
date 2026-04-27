package com.cinx.course.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateAssignmentLessonRequest {
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private List<AttachmentDto> attachments;

    @Data
    public static class AttachmentDto {
        private String fileKey;
        private String fileName;
        private String fileType;
        private Long fileSize;
    }
}
