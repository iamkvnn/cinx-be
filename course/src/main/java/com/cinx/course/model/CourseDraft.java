package com.cinx.course.model;

import com.cinx.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDraft extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    private String title;
    @Column(length = 2000)
    private String description;
    private Long duration;
    private Long price;
    private Long discountedPrice;
    private Long discountRate;
    private Boolean isInSubscription;
    private Boolean hasCertificate;
    private String certificateTitle;

    @ManyToOne
    private Category category;

}
