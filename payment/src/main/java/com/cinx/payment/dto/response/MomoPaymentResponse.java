package com.cinx.payment.dto.response;

public record MomoPaymentResponse (
        Long transactionId,
        String partnerCode,
        String requestId,
        Long amount,
        String responseTime,
        String message,
        int resultCode,
        String signature
) {
}
