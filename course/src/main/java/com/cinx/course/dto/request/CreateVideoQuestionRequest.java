package com.cinx.course.dto.request;

import com.cinx.course.consts.VideoQuizQuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateVideoQuestionRequest(
        @NotBlank
        @Schema(example = "What is the primary role of Spring Boot?")
        String questionText,
        @NotNull
        @Schema(example = "SINGLE_CHOICE")
        VideoQuizQuestionType questionType,
        @NotNull
        @Min(0)
        @Schema(example = "120")
        Integer timestampSeconds,
        @NotEmpty
        @Valid
        List<CreateVideoOptionRequest> options
) {
}
