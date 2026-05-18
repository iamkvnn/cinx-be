package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record CourseResponse(
        @Schema(example = "course_123")
        String id,
        @Schema(example = "Java Programming 101")
        String title,
        @Schema(example = "Comprehensive guide to Java")
        String description,
        CategoryResponse category,
        InstructorResponse instructor,
        List<CourseImageResponse> images,
        @Schema(example = "199000")
        Long price,
        @Schema(example = "159000")
        Long discountedPrice,
        @Schema(example = "20")
        Long discountRate,
        @Schema(example = "4.8")
        Double rating,
        @Schema(example = "2500")
        Long enrollmentCount,
        @Schema(example = "true")
        Boolean isPublished,
        @Schema(example = "false")
        Boolean isInSubscription,
        @Schema(example = "3600")
        Long duration,
        @Schema(example = "2025-01-01T10:00:00")
        LocalDateTime createdAt,
        @Schema(example = "2025-01-01T10:00:00")
        LocalDateTime updatedAt
) {
}
