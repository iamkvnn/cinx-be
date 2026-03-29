package com.cinx.course.dto.request;

import com.cinx.course.consts.LessonType;

public record CreateLessonRequest (
     String title,
     Long duration,
     Integer orderIndex,
     LessonType lessonType
) {
}

