package com.cinx.course.dto.request;

import java.util.List;

public record UpdateCourseRequest(
        String title,
        String description,
        String categoryId,
        Long price,
        Long discountedPrice,
        Boolean isPublished,
        Boolean isInSubscription,
        Long duration,
        Boolean hasCertificate,
        String certificateTitle,
        List<UpdateSectionRequest> sections
) {
}
