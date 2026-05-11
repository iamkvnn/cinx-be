package com.cinx.enrollment.model;

import com.cinx.common.model.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher extends AuditableEntity {
    private String code;
    private Long discountAmount;
    private Long minPurchaseAmount;
    private Long maxDiscountAmount;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String description;
    private Long quantity;
    private Boolean active;

    @OneToOne(fetch = FetchType.LAZY)
    private Order order;
}
