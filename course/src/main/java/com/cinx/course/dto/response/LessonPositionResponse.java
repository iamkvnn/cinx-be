package com.cinx.course.dto.response;

public record LessonPositionResponse(
        String lessonId,
        String sectionId,
        Integer orderIndex
) {
}
