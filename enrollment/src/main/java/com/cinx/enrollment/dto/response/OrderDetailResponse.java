package com.cinx.enrollment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse (
    String id,
    String userId,
    List<OrderItemResponse> items,
    Long totalPrice,
    Long discounted,
    LocalDateTime orderDate,
    PaymentResponse payment,
    VoucherResponse voucher
) {}
