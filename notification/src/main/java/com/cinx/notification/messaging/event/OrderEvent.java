package com.cinx.notification.messaging.event;

import com.cinx.notification.consts.OrderStatus;
import com.cinx.notification.consts.PaymentMethod;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderEvent {
    private String id;
    private String userId;
    private Long totalPrice;
    private Long discounted;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
}
