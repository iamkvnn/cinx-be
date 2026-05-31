package com.cinx.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CartItemResponse (
    @Schema(example = "cart_item_123")
    String id,
    CourseResponse course
) {
}
