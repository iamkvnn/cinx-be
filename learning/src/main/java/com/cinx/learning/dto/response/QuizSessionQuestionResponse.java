package com.cinx.learning.dto.response;

import com.cinx.learning.consts.QuizQuestionType;

public record QuizSessionQuestionResponse(
        String id,
        String quizSessionId,
        String questionId,
        QuizQuestionType questionType,
        Integer questionOrder,
        String userAnswer
) {
}
