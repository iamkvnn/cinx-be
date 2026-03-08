package com.cinx.enrollment.dto.response;

import com.cinx.enrollment.consts.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
     String id,
     Long amount,
     PaymentStatus status,
     LocalDateTime paymentDate,
     String paymentInfo,
     String paymentMessage
) {
}
