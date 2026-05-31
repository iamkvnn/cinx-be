package com.cinx.learning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record CourseDetailResponse (
        @Schema(example = "course_123")
        String id,
        @Schema(example = "Advanced Java")
        String title,
        @Schema(example = "Learn advanced Java topics")
        String description,
        CategoryResponse category,
        InstructorResponse instructor,
        List<CourseImageResponse> images,
        @Schema(example = "1000")
        Long price,
        @Schema(example = "800")
        Long discountedPrice,
        @Schema(example = "20")
        Long discountRate,
        @Schema(example = "4.8")
        Double rating,
        @Schema(example = "150")
        Long enrollmentCount,
        @Schema(example = "true")
        Boolean isPublished,
        @Schema(example = "true")
        Boolean isInSubscription,
        @Schema(example = "7200")
        Long duration,
        @Schema(example = "true")
        Boolean hasCertificate,
        @Schema(example = "Java Expert Certificate")
        String certificateTitle,
        @Schema(example = "2025-01-01T10:00:00")
        LocalDateTime createdAt,
        @Schema(example = "2025-01-01T10:00:00")
        LocalDateTime updatedAt,
        List<SectionResponse> sections
) {
}
