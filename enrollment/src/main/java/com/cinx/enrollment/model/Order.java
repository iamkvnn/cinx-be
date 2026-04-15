package com.cinx.enrollment.model;

import com.cinx.common.model.BaseEntity;
import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.consts.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order extends BaseEntity {
    private String userId;
    private Long totalPrice;
    private Long discounted;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private String voucherId;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "voucher_id", insertable = false, updatable = false)
    private Voucher voucher;

    @OneToMany(mappedBy = "orderId")
    private List<OrderItem> items;
}
