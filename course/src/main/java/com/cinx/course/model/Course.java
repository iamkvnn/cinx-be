package com.cinx.course.model;

import com.cinx.common.model.BaseEntity;
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
public class Course extends BaseEntity {
    private String title;
    @Column(length = 2000)
    private String description;
    private Long duration;
    private Long price;
    private Long discountedPrice;
    private Long discountRate;
    private Double rating;
    private Long enrollmentCount;
    private Boolean isPublished;
    private Boolean isInSubscription;

    @ManyToOne
    private Category category;

    @OneToMany
    private List<Section> sections = new ArrayList<>();

    @ManyToOne
    private Instructor instructor;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<CourseImage> images = new ArrayList<>();
}
