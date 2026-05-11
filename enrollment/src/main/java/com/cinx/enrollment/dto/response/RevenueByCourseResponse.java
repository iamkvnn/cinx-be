package com.cinx.enrollment.dto.response;

public record RevenueByCourseResponse(
        String courseId,
        String title,
        Long enrollments,
        Long grossRevenue,
        Long netRevenue
) {
}