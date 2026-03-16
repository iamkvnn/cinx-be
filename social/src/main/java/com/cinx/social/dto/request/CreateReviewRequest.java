package com.cinx.social.dto.request;

public record CreateReviewRequest(
        String courseId,
        String content,
        Double rating
) {
}
