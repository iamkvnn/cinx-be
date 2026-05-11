package com.cinx.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateAssignmentLessonRequest {
        @NotBlank(message = "Description is required")
        private String description;
        
        @Valid
        private List<AttachmentDto> attachments;

        @Data
        public static class AttachmentDto {
                @NotBlank(message = "File key is required")
                private String fileKey;
                
                @NotBlank(message = "File name is required")
                private String fileName;
                
                @NotBlank(message = "File type is required")
                private String fileType;
                
                @NotNull(message = "File size is required")
                @Min(value = 0, message = "File size must be non-negative")
                private Long fileSize;
        }
}
