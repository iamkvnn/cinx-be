package com.cinx.enrollment.dto.response;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.consts.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse (
        @Schema(example = "ord_123")
        String id,
        @Schema(example = "user_123")
        String userId,
        List<OrderItemResponse> items,
        @Schema(example = "150000")
        Long totalPrice,
        @Schema(example = "50000")
        Long discounted,
        @Schema(example = "2025-01-01T10:00:00")
        LocalDateTime orderDate,
        @Schema(example = "COMPLETED")
        OrderStatus status,
        @Schema(example = "VNPAY")
        PaymentMethod paymentMethod,
        PaymentResponse payment,
        VoucherResponse voucher
) {}
