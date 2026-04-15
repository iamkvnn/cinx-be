package com.cinx.auth.dto.response;

public record GoogleProfileResponse(
        String email,
        String name,
        String picture
) {
}
