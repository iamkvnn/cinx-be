package com.cinx.course.dto.request;

public record CreateCourseRequest (
        String title,
        String description,
        String categoryId,
        Long price,
        Long discountedPrice,
        Boolean isInSubscription,
        Long duration,
        Boolean hasCertificate,
        String certificateTitle
) {
}
