package com.cinx.enrollment.dto.response;

public record CourseStats(
        String courseId,
        String title,
        Long enrollmentCount
) {}
