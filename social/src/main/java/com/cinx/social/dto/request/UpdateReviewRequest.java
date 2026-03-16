package com.cinx.social.dto.request;

public record UpdateReviewRequest(
        String content,
        Double rating
) {
}
