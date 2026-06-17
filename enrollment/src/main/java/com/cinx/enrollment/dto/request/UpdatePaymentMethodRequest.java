package com.cinx.enrollment.dto.request;

import com.cinx.enrollment.consts.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentMethodRequest(
        @NotNull(message = "paymentMethod must not be null")
        @Schema(example = "STRIPE")
        PaymentMethod paymentMethod
) {
}
