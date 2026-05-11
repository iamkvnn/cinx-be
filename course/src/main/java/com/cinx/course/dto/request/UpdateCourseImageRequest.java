package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCourseImageRequest {
    @NotBlank
    private String fileKey;
}
