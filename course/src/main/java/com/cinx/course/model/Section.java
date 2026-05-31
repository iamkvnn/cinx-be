package com.cinx.course.model;

import com.cinx.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Section extends AuditableEntity {
    @Column(nullable = false)
    private String stableId;

    private String title;
    private String description;
    private Long duration;
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course; // Published course

    @ManyToOne(fetch = FetchType.LAZY)
    private CourseDraft draft; // Draft course

    @Builder.Default
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();
}
