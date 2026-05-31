package com.cinx.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LessonPreviewResponse (
        LessonResponse lesson,
        Object content
) {
}