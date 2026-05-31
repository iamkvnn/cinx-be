package com.cinx.course.dto.request;

import com.cinx.course.consts.ScoringMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateQuizQuestionRequest(
        @NotBlank
        @Schema(example = "Updated quiz question text")
        String questionText,
        @NotNull
        @Schema(example = "PARTIAL")
        ScoringMethod scoringMethod,
        @Valid
        List<UpdateQuizOptionRequest> options
) {}
