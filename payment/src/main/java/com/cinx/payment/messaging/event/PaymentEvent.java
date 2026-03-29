package com.cinx.payment.messaging.event;

import com.cinx.payment.consts.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentEvent {
    private String orderId;
    private PaymentStatus status;
}
