package com.cinx.social.dto.response;

public record ReviewReactionResponse(
        String id,
        String userId,
        String reviewId,
        boolean liked
) {
}
