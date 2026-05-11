package com.cinx.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateVideoQuestionRequest(
        @NotBlank
        String questionText,
        @Min(0)
        Integer timestampSeconds,
        @Valid
        List<UpdateVideoOptionRequest> options
) {
}