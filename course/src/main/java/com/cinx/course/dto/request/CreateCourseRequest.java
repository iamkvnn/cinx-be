package com.cinx.course.dto.request;

import java.util.List;

public record CreateCourseRequest (
        String title,
        String description,
        String categoryId,
        String instructorId,
        Long price,
        Long discountedPrice,
        Boolean isPublished,
        Boolean isInSubscription,
        Long duration,
        List<CreateSectionRequest> sections
) {
}
