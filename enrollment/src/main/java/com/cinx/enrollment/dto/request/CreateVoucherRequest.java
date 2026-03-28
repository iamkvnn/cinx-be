package com.cinx.enrollment.dto.request;

import java.time.LocalDateTime;

public record CreateVoucherRequest(
        String code,
        Long discountAmount,
        Long minPurchaseAmount,
        String maxDiscountAmount,
        String description,
        Long quantity,
        LocalDateTime validFrom,
        LocalDateTime validTo
) {
}
