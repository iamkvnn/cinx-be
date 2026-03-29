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
public class QuizLesson {
    @Id
    private String lessonId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer numberOfQuestionPerQuizSession;
    private Integer maxAttempt;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessonId", insertable = false, updatable = false)
    private Lesson lesson;

    @OneToMany(mappedBy = "quizLesson")
    private List<QuizQuestion> questions;
}
