package com.cinx.social.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateReviewReactionRequest(
    @NotNull(message = "liked must not be null")
    Boolean liked
) {
}
