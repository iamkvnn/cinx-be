package com.cinx.enrollment.dto.response;

public record CourseResponse(
        String id,
        String title,
        Long price,
        Long discountedPrice
) {
}
