package com.cinx.enrollment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;


import com.cinx.enrollment.consts.PaymentMethod;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "cartItems must not be empty") @Valid
        List<CartItemDto> cartItems,

        @NotNull(message = "paymentMethod must not be null")
        @Schema(example = "VNPAY")
        PaymentMethod paymentMethod,

        @Schema(example = "SUMMER2025")
        String voucherCode
) {
}
