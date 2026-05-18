package com.cinx.payment.dto.request;

import com.cinx.payment.consts.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentRequest(
    @Schema(example = "order_123")
    String orderId,
    @Schema(example = "MOMO")
    PaymentMethod paymentMethod
) {
}
