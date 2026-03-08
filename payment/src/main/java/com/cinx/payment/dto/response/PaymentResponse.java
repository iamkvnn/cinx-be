package com.cinx.payment.dto.response;

import com.cinx.payment.consts.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse (
     String id,
     Long amount,
     PaymentStatus status,
     LocalDateTime paymentDate,
     String paymentInfo,
     String paymentMessage
) {
}
