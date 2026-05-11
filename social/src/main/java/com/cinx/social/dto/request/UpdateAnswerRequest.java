package com.cinx.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAnswerRequest {
    @NotBlank
    private String content;
}
