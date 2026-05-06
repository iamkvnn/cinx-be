package com.cinx.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateQuestionRequest {
    @NotBlank
    private String courseId;

    private String lessonId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
