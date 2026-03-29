package com.cinx.enrollment.messaging.event;

import com.cinx.enrollment.consts.PaymentStatus;
import lombok.Data;

@Data
public class PaymentEvent {
    private String orderId;
    private PaymentStatus status;
}
