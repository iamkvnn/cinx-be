package com.cinx.enrollment.dto.request;
import jakarta.validation.constraints.NotBlank;


public record CreateEnrolledCourseRequest(
    @NotBlank(message = "courseId must not be blank")
    String courseId,

    @NotBlank(message = "userId must not be blank")
    String userId
) {
}
