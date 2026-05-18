package com.cinx.social.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateReviewReplyRequest {
    @NotBlank
    @Schema(example = "Updated reply content")
    private String content;
}
