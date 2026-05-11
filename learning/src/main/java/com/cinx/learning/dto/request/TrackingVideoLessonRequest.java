package com.cinx.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrackingVideoLessonRequest(
        @NotBlank(message = "videoLessonId must not be blank")
        String videoLessonId,

        @NotNull(message = "currentPosition must not be null")
        Integer currentPosition
) {
}
