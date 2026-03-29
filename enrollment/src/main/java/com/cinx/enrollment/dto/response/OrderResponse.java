package com.cinx.enrollment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        String id,
        String userId,
        List<OrderItemResponse> items,
        Long totalPrice,
        Long discounted,
        LocalDateTime orderDate
) {
}
