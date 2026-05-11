package com.cinx.enrollment.dto.response;

import java.time.LocalDateTime;

public record VoucherResponse(
        String id,
        String code,
        Long discountAmount,
        Long minPurchaseAmount,
        Long maxDiscountAmount,
        String description,
        Long quantity,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
