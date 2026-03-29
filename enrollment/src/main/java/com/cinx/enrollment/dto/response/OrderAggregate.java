package com.cinx.enrollment.dto.response;

import com.cinx.enrollment.model.Order;

public record OrderAggregate(
        Order order,
        PaymentResponse payment
) {}
