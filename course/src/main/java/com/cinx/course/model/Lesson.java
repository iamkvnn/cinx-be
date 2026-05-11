package com.cinx.course.model;

import com.cinx.common.model.AuditableEntity;
import com.cinx.course.consts.LessonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends AuditableEntity {

    private String title;
    private Long duration;
    private Integer orderIndex;
    private LessonType lessonType;
    @Column(columnDefinition = "boolean default false")
    private Boolean isPreview;

    @ManyToMany
    @JoinTable(name = "lesson_prerequisites",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id"))
    private List<Lesson> prerequisites;

    @ManyToOne
    private Section section;

    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private VideoLesson videoLesson;

    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private ArticleLesson articleLesson;

    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private QuizLesson quizLesson;

    @OneToOne(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private AssignmentLesson assessmentLesson;
}
