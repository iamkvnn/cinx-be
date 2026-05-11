package com.cinx.course.dto.request;

import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.ScoringMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuizQuestionRequest(
        @NotBlank
        String questionText,
        @NotNull
        QuizQuestionType questionType,
        @NotNull
        ScoringMethod scoringMethod,
        @NotEmpty
        @Valid
        List<CreateQuizOptionRequest> options
) {
}
