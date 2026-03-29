package com.cinx.enrollment.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {
    private String courseId;
    private String orderId;
    private String title;
    private Long price;
    private Long discountedPrice;
}
