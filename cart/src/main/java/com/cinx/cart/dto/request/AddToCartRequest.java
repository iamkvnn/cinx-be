package com.cinx.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AddToCartRequest(
    @NotBlank(message = "Course ID is required")
    @Schema(example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    String courseId
) {
}
