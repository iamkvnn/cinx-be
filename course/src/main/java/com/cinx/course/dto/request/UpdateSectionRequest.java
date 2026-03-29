package com.cinx.course.dto.request;

import java.util.List;

public record UpdateSectionRequest(
        String id,
        String title,
        String description,
        Long duration,
        Integer orderIndex,
        List<UpdateLessonRequest> lessons
) {
}
