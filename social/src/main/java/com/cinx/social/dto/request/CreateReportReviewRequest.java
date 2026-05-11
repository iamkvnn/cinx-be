package com.cinx.social.dto.request;
import jakarta.validation.constraints.NotBlank;


public record CreateReportReviewRequest(
    @NotBlank(message = "reason must not be blank")
    String reason
) {
}
