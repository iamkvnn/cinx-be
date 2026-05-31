package com.cinx.course.dto.response;

import com.cinx.course.consts.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

public record InstructorResponse(
        @Schema(example = "usr_123")
        String id,
        @Schema(example = "Nguyen Van A")
        String name,
        @Schema(example = "instructor@gmail.com")
        String email,
        @Schema(example = "MALE")
        Gender gender,
        @Schema(example = "https://example.com/profiles/123.jpg")
        String avatarUrl
) {
}
