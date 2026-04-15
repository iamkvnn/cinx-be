package com.cinx.enrollment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CourseResponse(
        String id,
        String title,
        String description,
        CategoryResponse category,
        InstructorResponse instructor,
        List<CourseImageResponse> images,
        Long price,
        Long discountedPrice,
        Long discountRate,
        Double rating,
        Long enrollmentCount,
        Boolean isPublished,
        Boolean isInSubscription,
        Long duration,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
