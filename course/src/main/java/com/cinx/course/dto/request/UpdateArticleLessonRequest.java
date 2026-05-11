package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateArticleLessonRequest {
    @NotBlank
    private String content;
}
