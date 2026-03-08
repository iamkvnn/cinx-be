package com.cinx.payment.dto.request;

import com.cinx.payment.consts.PaymentMethod;

public record PaymentRequest(
    String orderId,
    PaymentMethod paymentMethod
) {
}
