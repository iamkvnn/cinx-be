package com.cinx.course.dto.request;

import com.cinx.common.validation.ValidDateRange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ValidDateRange
public class UpdateAssignmentLessonRequest {
    private String description;
    
    private LocalDateTime startDate;
    
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
    
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
        private Long fileSize;
    }
}
