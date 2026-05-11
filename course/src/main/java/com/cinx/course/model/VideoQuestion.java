package com.cinx.course.model;

import com.cinx.common.model.BaseEntity;
import com.cinx.course.consts.QuizQuestionType;
import com.cinx.course.consts.VideoQuizQuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VideoQuestion extends BaseEntity {
    
    private String questionText;
    
    @Enumerated(EnumType.STRING)
    private VideoQuizQuestionType questionType;
    
    private Integer timestampSeconds;

    @OneToMany(mappedBy = "videoQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VideoOption> options;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessonId")
    private VideoLesson videoLesson;
}
