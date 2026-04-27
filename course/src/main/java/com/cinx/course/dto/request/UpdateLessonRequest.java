package com.cinx.course.dto.request;

public record UpdateLessonRequest(
     String title,
     Long duration,
     Integer orderIndex
) {
}

