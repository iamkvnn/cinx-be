package com.cinx.enrollment.dto.request;

import com.cinx.common.validation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@ValidDateRange(startDateField = "validFrom", dueDateField = "validTo", message = "validTo must be strictly after validFrom")
public record CreateVoucherRequest(
        @NotBlank(message = "Code is required")
        @Schema(example = "SUMMER2025")
        String code,
        
        @NotNull(message = "Discount amount is required")
        @PositiveOrZero(message = "Discount amount must be positive or zero")
        @Schema(example = "50000")
        Long discountAmount,
        
        @NotNull(message = "Min purchase amount is required")
        @PositiveOrZero(message = "Min purchase amount must be positive or zero")
        @Schema(example = "200000")
        Long minPurchaseAmount,

        @NotNull(message = "Max discount amount is required")
        @PositiveOrZero(message = "Max discount amount must be positive or zero")
        @Schema(example = "100000")
        Long maxDiscountAmount,
        
        @Schema(example = "Summer discount voucher")
        String description,
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(example = "100")
        Long quantity,
        
        @NotNull(message = "Valid from datetime is required")
        @Schema(example = "2025-06-01T00:00:00")
        LocalDateTime validFrom,
        
        @NotNull(message = "Valid to datetime is required")
        @Schema(example = "2025-08-31T23:59:59")
        LocalDateTime validTo
) {
}
