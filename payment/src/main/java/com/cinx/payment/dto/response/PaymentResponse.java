package com.cinx.payment.dto.response;

import com.cinx.payment.consts.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PaymentResponse (
     @Schema(example = "pay_123")
     String id,
     @Schema(example = "ord_123")
     String orderId,
     @Schema(example = "150000")
     Long amount,
     @Schema(example = "MOMO")
     PaymentStatus status,
     @Schema(example = "2025-01-01T10:00:00")
     LocalDateTime paymentDate,
     @Schema(example = "Payment processed successfully")
     String paymentInfo,
     @Schema(example = "Payment successful")
     String paymentMessage
) {
}
