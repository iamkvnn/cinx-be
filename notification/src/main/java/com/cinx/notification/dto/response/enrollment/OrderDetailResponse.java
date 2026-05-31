package com.cinx.notification.dto.response.enrollment;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderDetailResponse(
        @Schema(example = "ord_123")
        String id,
        @Schema(example = "user_123")
        String userId,
        @Schema(example = "150000")
        Long totalPrice,
        @Schema(example = "50000")
        Long discounted
) {}