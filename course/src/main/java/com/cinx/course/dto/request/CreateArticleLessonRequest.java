package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateArticleLessonRequest {
    @NotBlank(message = "Content is required")
    private String content;
}
