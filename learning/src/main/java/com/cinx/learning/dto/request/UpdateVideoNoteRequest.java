package com.cinx.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;


import lombok.Data;

@Data
public class UpdateVideoNoteRequest {
    @NotBlank(message = "content must not be blank")
    private String content;

    @NotNull(message = "videoTimestamp must not be null")
    @Min(value = 0, message = "videoTimestamp must be greater than or equal to 0")
    private Integer videoTimestamp;
}
