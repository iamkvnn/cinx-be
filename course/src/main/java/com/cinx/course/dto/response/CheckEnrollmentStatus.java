package com.cinx.course.dto.response;

public record CheckEnrollmentStatus(
        String courseId,
        Boolean enrolled
) {
}
