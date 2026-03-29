package com.cinx.course.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateAssignmentLessonRequest {
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime dueDate;
        private List<AttachmentDto> attachments;

        @Data
        public static class AttachmentDto {
                private String attachmentUrl;
                private String s3ObjectKey;
                private String fileName;
                private String fileType;
                private Long fileSize;
        }
}
