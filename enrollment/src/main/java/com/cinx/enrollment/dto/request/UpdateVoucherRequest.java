package com.cinx.enrollment.dto.request;

import com.cinx.common.validation.ValidDateRange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@ValidDateRange(startDateField = "validFrom", dueDateField = "validTo", message = "validTo must be strictly after validFrom")
public record UpdateVoucherRequest(
        String code,
        
        @PositiveOrZero(message = "Discount amount must be positive or zero")
        Long discountAmount,
        
        @PositiveOrZero(message = "Min purchase amount must be positive or zero")
        Long minPurchaseAmount,

        @PositiveOrZero(message = "Max discount amount must be positive or zero")
        Long maxDiscountAmount,
        String description,
        
        @Min(value = 1, message = "Quantity must be at least 1")
        Long quantity,
        
        LocalDateTime validFrom,
        LocalDateTime validTo
) {
}
