package com.cinx.learning.dto.response;


public record LessonResponse (
        String id,
        String title,
        String description,
        Long duration,
        Integer orderIndex
) {
}

