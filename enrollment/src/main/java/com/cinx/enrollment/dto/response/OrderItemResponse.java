package com.cinx.enrollment.dto.response;

public record OrderItemResponse(
        String id,
        String courseId,
        String title,
        Long price,
        Long discountedPrice
) {
}
