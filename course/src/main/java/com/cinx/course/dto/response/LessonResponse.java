package com.cinx.course.dto.response;


import com.cinx.course.consts.LessonType;

import java.time.LocalDateTime;

public record LessonResponse (
        String id,
        String title,
        Long duration,
        LessonType lessonType,
        Integer orderIndex,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
