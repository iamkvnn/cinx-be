package com.cinx.learning.dto.response;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.ScoringMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record QuizQuestionResponse(
        @Schema(example = "q_123")
        String id,
        @Schema(example = "What is encapsulation?")
        String questionText,
        @Schema(example = "MULTIPLE_CHOICE")
        QuizQuestionType questionType,
        @Schema(example = "PARTIAL")
        ScoringMethod scoringMethod,
        List<QuizOptionResponse> options
) {
}
