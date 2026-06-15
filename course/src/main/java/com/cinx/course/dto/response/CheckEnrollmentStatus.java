package com.cinx.course.dto.response;

public record CheckEnrollmentStatus(
        String courseId,
        boolean isEnrolled
) {
}
