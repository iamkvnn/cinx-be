package com.cinx.course.dto.request;

import com.cinx.course.consts.LessonType;

import java.util.List;

public record CreateLessonRequest (
     String title,
     Long duration,
     Integer orderIndex,
     LessonType lessonType,
     Boolean isPreview,
     List<String> prerequisiteIds
) {
}

