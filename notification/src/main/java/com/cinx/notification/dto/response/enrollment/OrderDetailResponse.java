package com.cinx.notification.dto.response.enrollment;

public record OrderDetailResponse(
        String id,
        String userId,
        Long totalPrice,
        Long discounted
) {}