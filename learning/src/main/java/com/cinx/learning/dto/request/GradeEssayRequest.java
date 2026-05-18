package com.cinx.learning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GradeEssayRequest(
        @NotNull
        @Valid
        List<EssayQuestionScore> scores
) {
    public record EssayQuestionScore(
            @Schema(example = "q_123")
            @NotNull String questionId,
            @Schema(example = "10.0")
            @NotNull Double score
    ) {}
}
