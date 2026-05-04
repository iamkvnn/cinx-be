package com.cinx.learning.dto.response;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.ScoringMethod;

public record QuizSessionQuestionResponse(
        String id,
        String quizSessionId,
        String questionId,
        QuizQuestionType questionType,
        Integer questionOrder,
        String userAnswer,
        String correctAnswer,
        Boolean isCorrect,
        Double score,
        ScoringMethod scoringMethod
) {
}
