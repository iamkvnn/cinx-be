package com.cinx.course.dto.response;

import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.ScoringMethod;

import java.util.List;

public record QuizQuestionResponse(
        String id,
        String questionText,
        QuizQuestionType questionType,
        Integer orderIndex,
        ScoringMethod scoringMethod,
        List<QuizOptionResponse> options
) {
}
