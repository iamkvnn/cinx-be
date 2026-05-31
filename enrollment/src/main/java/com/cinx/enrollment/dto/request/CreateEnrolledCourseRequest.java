package com.cinx.enrollment.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


public record CreateEnrolledCourseRequest(
    @NotBlank(message = "courseId must not be blank")
    @Schema(example = "course_123")
    String courseId,

    @NotBlank(message = "userId must not be blank")
    @Schema(example = "user_123")
    String userId
) {
}
