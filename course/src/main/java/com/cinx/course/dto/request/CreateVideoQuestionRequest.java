package com.cinx.course.dto.request;

import com.cinx.course.consts.VideoQuizQuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateVideoQuestionRequest(
        @NotBlank
        String questionText,
        @NotNull
        VideoQuizQuestionType questionType,
        @NotNull
        @Min(0)
        Integer timestampSeconds,
        @NotEmpty
        @Valid
        List<CreateVideoOptionRequest> options
) {
}
