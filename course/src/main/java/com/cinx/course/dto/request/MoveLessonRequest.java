package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MoveLessonRequest(
        @NotBlank
        String targetSectionId,
        String previousLessonId,
        String nextLessonId
) {
}
