package com.cinx.enrollment.dto.response;

import com.cinx.enrollment.consts.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PaymentResponse(
     @Schema(example = "pay_123")
     String id,
     @Schema(example = "ord_123")
     String orderId,
     @Schema(example = "100000")
     Long amount,
     @Schema(example = "SUCCESS")
     PaymentStatus status,
     @Schema(example = "2025-01-01T10:00:00")
     LocalDateTime paymentDate,
     @Schema(example = "Payment from VNPay")
     String paymentInfo,
     @Schema(example = "Success")
     String paymentMessage
) {
}
