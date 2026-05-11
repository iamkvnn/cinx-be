package com.cinx.social.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record CreateReviewRequest(
        @NotBlank(message = "courseId must not be blank")
        String courseId,

        @NotBlank(message = "content must not be blank")
        String content,

        @NotNull(message = "rating must not be null")
        Double rating
) {
}
