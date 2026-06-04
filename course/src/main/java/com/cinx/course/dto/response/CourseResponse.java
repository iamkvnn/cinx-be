package com.cinx.course.dto.response;

import com.cinx.course.consts.CoursePublishStatus;
import com.cinx.course.consts.CourseStatus;

import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

public record CourseResponse (
        @Schema(example = "course_abc123")
        String id,
        @Schema(example = "Spring Boot Microservices")
        String title,
        @Schema(example = "Learn how to build microservices with Spring Boot 3")
        String description,
        CategoryResponse category,
        InstructorResponse instructor,
        List<CourseImageResponse> images,
        @Schema(example = "1000000")
        Long price,
        @Schema(example = "800000")
        Long discountedPrice,
        @Schema(example = "20")
        Long discountRate,
        @Schema(example = "4.8")
        Double rating,
        @Schema(example = "150")
        Long enrollmentCount,
        @Schema(example = "false")
        Boolean isInSubscription,
        @Schema(example = "120")
        Long duration,
        @Schema(example = "true")
        Boolean hasCertificate,
        @Schema(example = "Srping Boot Expert")
        String certificateTitle,
        @Schema(example = "PUBLISHED")
        CourseStatus status,
        @Schema(example = "WAITING_APPROVAL")
        CoursePublishStatus publishStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
