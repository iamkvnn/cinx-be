package com.cinx.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAnswerRequest {
    @NotBlank
    private String questionId;

    private String parentAnswerId;

    @NotBlank
    private String content;
}
