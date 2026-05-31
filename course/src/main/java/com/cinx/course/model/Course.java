package com.cinx.course.model;

import com.cinx.common.model.AuditableEntity;
import com.cinx.course.consts.CourseStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends AuditableEntity {
    private String title;
    @Column(length = 2000)
    private String description;
    private Long duration;
    private Long price;
    private Long discountedPrice;
    private Long discountRate;
    private Double rating;
    private Long enrollmentCount;
    private Boolean isInSubscription;
    private String instructorId;

    @Builder.Default
    private Boolean hasCertificate = false;
    private String certificateTitle;
    private CourseStatus status;
    @Builder.Default
    private Boolean isPublished = false;

    @ManyToOne
    private Category category;

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<CourseImage> images = new ArrayList<>();
}
