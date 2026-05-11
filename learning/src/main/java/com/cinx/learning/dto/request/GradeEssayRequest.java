package com.cinx.learning.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GradeEssayRequest(
        @NotNull
        @Valid
        List<EssayQuestionScore> scores
) {
    public record EssayQuestionScore(
            @NotNull String questionId,
            @NotNull Double score
    ) {}
}
