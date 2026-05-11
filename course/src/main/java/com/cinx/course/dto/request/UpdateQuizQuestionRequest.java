package com.cinx.course.dto.request;

import com.cinx.course.consts.ScoringMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateQuizQuestionRequest(
        @NotBlank
        String questionText,
        @NotNull
        ScoringMethod scoringMethod,
        @Valid
        List<UpdateQuizOptionRequest> options
) {}
