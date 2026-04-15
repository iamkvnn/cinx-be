package com.cinx.payment.messaging.event;

import com.cinx.payment.consts.OrderStatus;
import com.cinx.payment.consts.PaymentMethod;
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
