package com.cinx.social.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record CourseResponse(
        @Schema(example = "course_123")
        String id,
        @Schema(example = "Introduction to Java")
        String title,
        @Schema(example = "Learn the basics of Java programming")
        String description,
        CategoryResponse category,
        InstructorResponse instructor,
        @Schema(example = "99000")
        Long price,
        @Schema(example = "59000")
        Long discountedPrice,
        @Schema(example = "40")
        Long discountRate,
        @Schema(example = "4.8")
        Double rating,
        @Schema(example = "1500")
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
