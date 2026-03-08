package com.cinx.cart.dto.response;

import java.time.LocalDateTime;

public record CourseResponse (
        String id,
        String title,
        String description,
        String category,
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
