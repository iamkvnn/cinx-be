package com.cinx.social.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateReviewReplyRequest {
    @NotBlank
    private String content;
}
