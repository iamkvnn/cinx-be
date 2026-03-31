package com.cinx.learning.dto.response;


import com.cinx.learning.consts.LessonType;

public record LessonResponse (
        String id,
        String title,
        Long duration,
        LessonType lessonType,
        Integer orderIndex
) {
}


