package com.cinx.course.dto.request;

import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.ScoringMethod;

import java.util.List;

public record CreateQuizQuestionRequest(
        String questionText,
        QuizQuestionType questionType,
        Integer orderIndex,
        ScoringMethod scoringMethod,
        List<CreateQuizOptionRequest> options
) {
}
