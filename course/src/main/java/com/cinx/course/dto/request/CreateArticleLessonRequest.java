package com.cinx.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateArticleLessonRequest {
    @NotBlank(message = "Content is required")
    @Schema(example = "<p>This is a Spring Boot tutorial article...</p>")
    private String content;
}
