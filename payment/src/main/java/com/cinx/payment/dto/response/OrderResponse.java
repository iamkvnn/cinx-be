package com.cinx.payment.dto.response;

import com.cinx.payment.consts.OrderStatus;
import com.cinx.payment.consts.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String id,
        String userId,
        List<OrderItemResponse> items,
        Long totalPrice,
        Long discounted,
        LocalDateTime orderDate,
        OrderStatus status,
        PaymentMethod paymentMethod
) {
}
