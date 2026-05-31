package com.cinx.learning.dto.response;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.ScoringMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record QuizSessionQuestionResponse(
        @Schema(example = "qseq_123")
        String id,
        @Schema(example = "sess_123")
        String quizSessionId,
        @Schema(example = "q_123")
        String questionId,
        @Schema(example = "MULTIPLE_CHOICE")
        QuizQuestionType questionType,
        @Schema(example = "PARTIAL")
        ScoringMethod scoringMethod,
        @Schema(example = "1")
        Integer questionOrder,
        @Schema(example = "What is polymorphism?")
        String questionText,
        @Schema(example = "opt_1")
        String userAnswer,
        @Schema(example = "opt_2")
        String correctAnswer, // null when hidden (IN_PROGRESS or hideAnswers policy)
        @Schema(example = "1.0")
        Double score, // null when hidden
        List<QuizSessionOptionResponse> options) {
}
