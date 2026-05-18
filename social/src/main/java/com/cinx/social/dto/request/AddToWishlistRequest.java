package com.cinx.social.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


public record AddToWishlistRequest(
    @NotBlank(message = "courseId must not be blank")
    @Schema(example = "course_123")
    String courseId
) {
}
