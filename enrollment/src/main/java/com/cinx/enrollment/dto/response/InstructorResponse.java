package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record InstructorResponse(
        @Schema(example = "inst_123")
        String id,
        @Schema(example = "John Doe")
        String name,
        @Schema(example = "johndoe@example.com")
        String email,
        @Schema(example = "Java Expert")
        String bio,
        @Schema(example = "avatar_url")
        String profilePictureUrl
) {
}
