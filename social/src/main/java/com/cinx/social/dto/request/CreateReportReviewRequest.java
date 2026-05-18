package com.cinx.social.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


public record CreateReportReviewRequest(
    @NotBlank(message = "reason must not be blank")
    @Schema(example = "Contains inappropriate content.")
    String reason
) {
}
