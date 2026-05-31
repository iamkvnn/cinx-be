package com.cinx.course.dto.response;

import java.util.List;

public record CourseCurriculumResponse(
        String courseId,
        List<CurriculumSectionResponse> sections
) {
}
