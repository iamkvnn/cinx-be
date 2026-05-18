package com.cinx.course.dto.request;

import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.ScoringMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuizQuestionRequest(
        @NotBlank
        @Schema(example = "What does SOLID stand for in Java?")
        String questionText,
        @NotNull
        @Schema(example = "SINGLE_CHOICE")
        QuizQuestionType questionType,
        @NotNull
        @Schema(example = "EXACT")
        ScoringMethod scoringMethod,
        @NotEmpty
        @Valid
        List<CreateQuizOptionRequest> options
) {
}
