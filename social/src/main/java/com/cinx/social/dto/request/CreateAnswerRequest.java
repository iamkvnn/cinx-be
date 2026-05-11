package com.cinx.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAnswerRequest {
    @NotBlank(message = "questionId must not be blank")
    private String questionId;

    private String parentAnswerId;

    @NotBlank(message = "content must not be blank")
    private String content;
}
