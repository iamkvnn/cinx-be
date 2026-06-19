package com.cinx.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class StripePayment extends Payment {
    @Column(unique = true)
    private String checkoutSessionId;
    private String paymentIntentId;
    private String stripePaymentStatus;
    @Column(unique = true)
    private String stripeEventId;
}
