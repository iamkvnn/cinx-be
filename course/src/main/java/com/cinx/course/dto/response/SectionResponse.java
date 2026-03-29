package com.cinx.course.dto.response;

import java.util.List;

public record SectionResponse(
        String id,
        String title,
        String description,
        Long duration,
        Integer orderIndex,
        List<LessonResponse> lessons
) {
}
