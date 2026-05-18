package com.cinx.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RejectCourseRequest(
        @NotBlank
        @Schema(example = "Course content violates copyright policies.")
        String reason
) {
}
