package com.cinx.course.dto.response;

import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.ScoringMethod;

import java.util.List;

public record QuizQuestionResponse(
        String id,
        String questionText,
        QuizQuestionType questionType,
        ScoringMethod scoringMethod,
        Boolean needSync,
        List<QuizOptionResponse> options
) {
}
