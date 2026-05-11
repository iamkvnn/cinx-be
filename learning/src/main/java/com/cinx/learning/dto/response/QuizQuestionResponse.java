package com.cinx.learning.dto.response;

import com.cinx.learning.consts.QuizQuestionType;
import com.cinx.learning.consts.ScoringMethod;

import java.util.List;

public record QuizQuestionResponse(
        String id,
        String questionText,
        QuizQuestionType questionType,
        ScoringMethod scoringMethod,
        List<QuizOptionResponse> options
) {
}
