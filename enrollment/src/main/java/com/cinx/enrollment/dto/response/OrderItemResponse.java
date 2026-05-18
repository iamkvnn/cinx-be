package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderItemResponse(
        @Schema(example = "item_123")
        String id,
        @Schema(example = "course_123")
        String courseId,
        @Schema(example = "Java Programming 101")
        String title,
        @Schema(example = "99000")
        Long price,
        @Schema(example = "59000")
        Long discountedPrice
) {
}
