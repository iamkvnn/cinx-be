package com.cinx.payment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
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
public class MomoPayment extends Payment {
    private String requestId;
    @Column(unique = true)
    private Long transactionId;
    private int resultCode;
}
