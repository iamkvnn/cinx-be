package com.cinx.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateArticleLessonRequest {
    @NotBlank
    @Schema(example = "<p>Updated Spring Boot tutorial content...</p>")
    private String content;
}
