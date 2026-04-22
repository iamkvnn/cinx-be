package com.cinx.course.dto.response;

import com.cinx.course.consts.CourseStatus;

import java.time.LocalDateTime;
import java.util.List;

public record CourseDetailResponse(
        String id,
        String title,
        String description,
        CategoryResponse category,
        InstructorResponse instructor,
        List<CourseImageResponse> images,
        Long price,
        Long discountedPrice,
        Long discountRate,
        Double rating,
        Long enrollmentCount,
        Boolean isInSubscription,
        Long duration,
        Boolean hasCertificate,
        String certificateTitle,
        CourseStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SectionResponse> sections
) {
}
