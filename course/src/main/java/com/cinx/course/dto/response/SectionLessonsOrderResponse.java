package com.cinx.course.dto.response;

import java.util.List;

public record SectionLessonsOrderResponse(
        String sectionId,
        List<LessonResponse> lessons
) {
}
