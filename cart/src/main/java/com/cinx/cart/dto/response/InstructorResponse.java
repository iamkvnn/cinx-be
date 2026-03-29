package com.cinx.cart.dto.response;

public record InstructorResponse(
        String id,
        String name,
        String email,
        String bio,
        String profilePictureUrl
) {
}
