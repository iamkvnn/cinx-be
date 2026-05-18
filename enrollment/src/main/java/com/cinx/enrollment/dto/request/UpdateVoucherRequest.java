package com.cinx.enrollment.dto.request;

import com.cinx.common.validation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@ValidDateRange(startDateField = "validFrom", dueDateField = "validTo", message = "validTo must be strictly after validFrom")
public record UpdateVoucherRequest(
        @Schema(example = "SUMMER2025_V2")
        String code,
        
        @PositiveOrZero(message = "Discount amount must be positive or zero")
        @Schema(example = "60000")
        Long discountAmount,
        
        @PositiveOrZero(message = "Min purchase amount must be positive or zero")
        @Schema(example = "250000")
        Long minPurchaseAmount,

        @PositiveOrZero(message = "Max discount amount must be positive or zero")
        @Schema(example = "120000")
        Long maxDiscountAmount,
        
        @Schema(example = "Updated summer discount voucher")
        String description,
        
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(example = "200")
        Long quantity,
        
        @Schema(example = "2025-06-01T00:00:00")
        LocalDateTime validFrom,
        
        @Schema(example = "2025-09-30T23:59:59")
        LocalDateTime validTo
) {
}
