package com.cinx.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ArticleLessonResponse (
        @Schema(example = "<p>This is a Spring Boot tutorial article...</p>")
        String content
){
}
