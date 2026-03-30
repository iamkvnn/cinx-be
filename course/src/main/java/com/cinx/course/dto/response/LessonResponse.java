package com.cinx.course.dto.response;


import com.cinx.course.consts.LessonType;

public record LessonResponse (
        String id,
        String title,
        String description,
        Long duration,
        LessonType lessonType,
        Integer orderIndex
) {
}

