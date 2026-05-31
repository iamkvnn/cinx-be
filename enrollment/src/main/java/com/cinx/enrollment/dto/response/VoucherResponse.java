package com.cinx.enrollment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record VoucherResponse(
        @Schema(example = "vou_123")
        String id,
        @Schema(example = "SUMMER2025")
        String code,
        @Schema(example = "50000")
        Long discountAmount,
        @Schema(example = "200000")
        Long minPurchaseAmount,
        @Schema(example = "100000")
        Long maxDiscountAmount,
        @Schema(example = "Summer discount voucher")
        String description,
        @Schema(example = "100")
        Long quantity,
        @Schema(example = "2025-06-01T00:00:00")
        LocalDateTime validFrom,
        @Schema(example = "2025-08-31T23:59:59")
        LocalDateTime validTo,
        @Schema(example = "2025-01-01T10:00:00")
        LocalDateTime createdAt,
        @Schema(example = "2025-01-01T10:00:00")
        LocalDateTime updatedAt
) {
}
