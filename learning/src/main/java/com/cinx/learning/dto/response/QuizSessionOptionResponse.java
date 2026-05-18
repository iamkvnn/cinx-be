package com.cinx.learning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record QuizSessionOptionResponse(
        @Schema(example = "opt_123")
        String id,
        @Schema(example = "Option text")
        String optionText,
        @Schema(example = "LEFT")
        String side
) {
}
