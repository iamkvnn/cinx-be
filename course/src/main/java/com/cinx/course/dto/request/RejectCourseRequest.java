package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectCourseRequest(
        @NotBlank
        String reason
) {
}
