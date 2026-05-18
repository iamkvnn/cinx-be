package com.cinx.course.dto.response;

import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.ScoringMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record QuizQuestionResponse(
        @Schema(example = "qq_123")
        String id,
        @Schema(example = "What does SOLID stand for in Java?")
        String questionText,
        @Schema(example = "SINGLE_CHOICE")
        QuizQuestionType questionType,
        @Schema(example = "EXACT")
        ScoringMethod scoringMethod,
        @Schema(example = "false")
        Boolean needSync,
        List<QuizOptionResponse> options
) {
}
