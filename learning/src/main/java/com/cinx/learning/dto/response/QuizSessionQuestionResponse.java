package com.cinx.learning.dto.response;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.ScoringMethod;

import java.util.List;

public record QuizSessionQuestionResponse(
        String id,
        String quizSessionId,
        String questionId,
        QuizQuestionType questionType,
        ScoringMethod scoringMethod,
        Integer questionOrder,
        String questionText,
        String userAnswer,
        String correctAnswer, // null when hidden (IN_PROGRESS or hideAnswers policy)
        Double score, // null when hidden
        List<QuizSessionOptionResponse> options) {
}
