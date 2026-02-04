package com.cinx.course.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity {
    private String title;
    @Column(length = 2000)
    private String description;
    private Long duration;
    private String imageUrl;
    private Long price;
    private Long discountedPrice;
    private Double rating;
    private Long enrollmentCount;
    private Boolean isPublished;
    private Boolean isInSubscription;

    @ManyToOne
    private Category category;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "course_tag",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Section> sections = new HashSet<>();

    @ManyToOne
    private Instructor instructor;
}
