package com.cinx.cart.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddToCartRequest(
    @NotBlank(message = "Course ID is required")
    String courseId
) {
}
