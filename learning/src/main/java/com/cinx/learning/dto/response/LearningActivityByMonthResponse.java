package com.cinx.learning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LearningActivityByMonthResponse(
        @Schema(example = "2026-05")
        String month,
        @Schema(example = "100800")
        Long activeSeconds
) {
}
