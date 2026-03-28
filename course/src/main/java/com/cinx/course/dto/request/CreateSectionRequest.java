package com.cinx.course.dto.request;

import java.util.List;

public record CreateSectionRequest(
        String title,
        String description,
        Long duration,
        Integer orderIndex,
        List<CreateLessonRequest> lessons
) {
}
