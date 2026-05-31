package com.cinx.learning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import lombok.Data;

@Data
public class UpdateVideoNoteRequest {
    @Schema(example = "Updated note content.")
    @NotBlank(message = "content must not be blank")
    private String content;

    @Schema(example = "150")
    @NotNull(message = "videoTimestamp must not be null")
    @Min(value = 0, message = "videoTimestamp must be greater than or equal to 0")
    private Integer videoTimestamp;
}
