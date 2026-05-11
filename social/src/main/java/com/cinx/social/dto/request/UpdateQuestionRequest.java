package com.cinx.social.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class UpdateQuestionRequest {
    @NotBlank(message = "title must not be blank")
    private String title;

    @NotBlank(message = "content must not be blank")
    private String content;
}
