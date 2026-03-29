package com.cinx.enrollment.messaging.event;

import com.cinx.enrollment.consts.PaymentMethod;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderEvent {
    private String id;
    private String userId;
    private Long totalPrice;
    private Long discounted;
    private LocalDateTime orderDate;
    private PaymentMethod paymentMethod;
}
