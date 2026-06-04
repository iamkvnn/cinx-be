package com.cinx.course.messaging.event;

import com.cinx.course.consts.CoursePublishStatus;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.response.CategoryResponse;
import com.cinx.course.dto.response.CourseImageResponse;
import com.cinx.course.dto.response.CurriculumSectionResponse;
import com.cinx.course.dto.response.InstructorResponse;

import java.time.LocalDateTime;
import java.util.List;

public record CourseRecommendationPayload(
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
        CoursePublishStatus publishStatus,
        List<CurriculumSectionResponse> sections,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
