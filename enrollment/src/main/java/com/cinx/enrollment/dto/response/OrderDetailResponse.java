package com.cinx.enrollment.dto.response;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.consts.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse (
        String id,
        String userId,
        List<OrderItemResponse> items,
        Long totalPrice,
        Long discounted,
        LocalDateTime orderDate,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentResponse payment,
        VoucherResponse voucher
) {}
