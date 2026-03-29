package com.cinx.course.dto.request;

public record UpdateLessonRequest(
     String id,
     String title,
     Long duration,
     Integer orderIndex
) {
}

