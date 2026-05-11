package com.cinx.social.dto.request;
import jakarta.validation.constraints.NotBlank;


public record AddToWishlistRequest(
    @NotBlank(message = "courseId must not be blank")
    String courseId
) {
}
