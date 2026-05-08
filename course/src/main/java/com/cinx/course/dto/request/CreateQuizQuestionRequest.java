package com.cinx.course.dto.request;

import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.ScoringMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuizQuestionRequest(
        String questionText,
        QuizQuestionType questionType,
        ScoringMethod scoringMethod,
        @NotEmpty
        List<CreateQuizOptionRequest> options
) {
}
