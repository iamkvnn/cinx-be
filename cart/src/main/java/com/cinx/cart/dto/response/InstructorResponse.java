package com.cinx.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record InstructorResponse(
        @Schema(example = "usr_123")
        String id,
        @Schema(example = "Nguyen Van A")
        String name,
        @Schema(example = "instructor@gmail.com")
        String email,
        @Schema(example = "Expert in Java and Spring Boot")
        String bio,
        @Schema(example = "https://example.com/profiles/123.jpg")
        String profilePictureUrl
) {
}
