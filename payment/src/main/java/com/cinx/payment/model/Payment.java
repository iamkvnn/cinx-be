package com.cinx.payment.model;

import com.cinx.payment.consts.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String id;
    protected Long amount;
    @Enumerated(EnumType.STRING)
    protected PaymentStatus status;
    protected LocalDateTime paymentDate;
    protected String paymentInfo;
    protected String paymentMessage;
    @Column(unique = true)
    protected String orderId;
    protected String paymentUrl;
    protected LocalDateTime urlExpireTime;
}
