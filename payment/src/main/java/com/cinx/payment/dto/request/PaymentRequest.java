package com.cinx.payment.dto.request;

import com.cinx.payment.consts.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
    @NotBlank(message = "orderId must not be blank")
    @Schema(example = "order_123")
    String orderId,
    @NotNull(message = "paymentMethod must not be null")
    @Schema(example = "MOMO")
    PaymentMethod paymentMethod
) {
}
