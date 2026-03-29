package com.cinx.course.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentLesson {
    @Id
    private String lessonId;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessonId")
    private Lesson lesson;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "assignment_lesson_id")
    private List<AssignmentAttachment> attachments;
}
