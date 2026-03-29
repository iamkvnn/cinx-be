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

    @OneToMany(mappedBy = "assignmentLesson", cascade = CascadeType.ALL)
    private List<AssignmentAttachment> attachments;
}
