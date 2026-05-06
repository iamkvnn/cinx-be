package com.cinx.course.dto.request;

import java.util.List;

public record UpdateLessonRequest(
     String title,
     Long duration,
     Integer orderIndex,
     Boolean isPreview,
     List<String> prerequisiteIds
) {
}

