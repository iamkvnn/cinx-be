package com.cinx.course.dto.request;

import com.cinx.course.consts.LessonType;

public record UpdateLessonRequest(
     String id,
     String title,
     Long duration,
     LessonType lessonType,
     Integer orderIndex
) {
}

