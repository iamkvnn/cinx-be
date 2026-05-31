package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SectionLessonsOrderRequest(
        @NotBlank
        String sectionId,
        @NotNull
        List<String> lessonIds
) {
}
