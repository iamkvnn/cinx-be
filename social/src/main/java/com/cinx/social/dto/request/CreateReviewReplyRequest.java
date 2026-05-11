package com.cinx.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateReviewReplyRequest {
    @NotBlank
    private String content;
}
