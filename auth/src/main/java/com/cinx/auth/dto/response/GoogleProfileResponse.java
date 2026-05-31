package com.cinx.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GoogleProfileResponse(
        @Schema(example = "nguyenvana@gmail.com")
        String email,
        @Schema(example = "Nguyen Van A")
        String name,
        @Schema(example = "https://lh3.googleusercontent.com/a/...")
        String picture
) {
}
