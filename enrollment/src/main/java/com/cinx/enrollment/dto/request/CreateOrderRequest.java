package com.cinx.enrollment.dto.request;

import com.cinx.enrollment.consts.PaymentMethod;

import java.util.List;

public record CreateOrderRequest(
        List<CartItemDto> cartItems,
        PaymentMethod paymentMethod,
        String voucherCode
) {
}
