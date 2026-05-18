package com.cinx.social.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record WishlistItemResponse(
        @Schema(example = "wish_123")
        String id,
        @Schema(example = "user_123")
        String userId,
        @Schema(example = "course_123")
        String courseId
) {
}
