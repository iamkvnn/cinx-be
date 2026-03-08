package com.cinx.enrollment.dto.response;

public record CheckEnrollmentStatus(
    String courseId,
    boolean isEnrolled
) {
}
