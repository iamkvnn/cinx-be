package com.cinx.enrollment.dto.request;

public record CreateEnrolledCourseRequest(
    String courseId,
    String userId
) {
}
