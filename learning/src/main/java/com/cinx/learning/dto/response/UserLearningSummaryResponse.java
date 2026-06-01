package com.cinx.learning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserLearningSummaryResponse(
        @Schema(example = "5")
        Long completedCourseCount,
        @Schema(example = "68.0")
        Double averageProgressPercent,
        @Schema(example = "446400")
        Long totalLearningSeconds
) {
}
