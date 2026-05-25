package com.cinx.course.model;

import com.cinx.common.model.AuditableEntity;
import com.cinx.course.consts.LessonType;
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
public class Lesson extends AuditableEntity {
    @Column(nullable = false)
    private String stableId;

    private String title;
    private Long duration;
    private Integer orderIndex;
    private LessonType lessonType;
    private Boolean isPreview;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "lesson_prerequisite_ids", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "prerequisite_id")
    private List<String> prerequisiteIds = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Section section;
}
