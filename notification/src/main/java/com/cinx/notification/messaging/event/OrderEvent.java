package com.cinx.enrollment.messaging.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderEvent {
    private String id;
    private Long totalPrice;
    private Long discounted;
    private LocalDateTime orderDate;
}
