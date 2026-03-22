package com.cinx.course.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CourseDetailResponse(
        String id,
        String title,
        String description,
        String category,
        InstructorResponse instructor,
        Long price,
        Long discountedPrice,
        Long discountRate,
        Double rating,
        Long enrollmentCount,
        Boolean isPublished,
        Boolean isInSubscription,
        Long duration,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SectionResponse> sections
) {
}
