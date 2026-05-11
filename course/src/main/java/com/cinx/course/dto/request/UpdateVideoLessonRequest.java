package com.cinx.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateVideoLessonRequest {
    @NotBlank
    private String fileKey;
    @NotBlank
    private String fileName;
    @NotBlank
    private String fileType;
    @Min(1)
    private Long fileSize;
    @Min(0)
    private Integer duration;
}
