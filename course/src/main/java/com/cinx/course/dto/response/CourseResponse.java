package com.cinx.course.dto.response;

import java.time.LocalDateTime;

public record CourseResponse (
        String id,
        String title,
        String description,
        String category,
        Double price,
        Long discountPrice,
        Double rating,
        Boolean isPublished,
        Boolean isInSubscription,
        Long duration,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
