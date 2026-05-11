package com.cinx.course.dto.response;

public record LessonPreviewResponse (
        LessonResponse lesson,
        Object content
) {
}