package com.cinx.course.dto.response;


import com.cinx.course.consts.LessonType;

import java.time.LocalDateTime;
import java.util.List;

public record LessonResponse (
        String id,
        String title,
        Long duration,
        LessonType lessonType,
        Integer orderIndex,
        Boolean isPreview,
        List<String> prerequisiteIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
