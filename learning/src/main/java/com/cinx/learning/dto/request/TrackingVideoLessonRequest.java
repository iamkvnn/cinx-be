package com.cinx.learning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrackingVideoLessonRequest(
        @Schema(example = "vlesson_123")
        @NotBlank(message = "videoLessonId must not be blank")
        String videoLessonId,

        @Schema(example = "120")
        @NotNull(message = "currentPosition must not be null")
        Integer currentPosition
) {
}
