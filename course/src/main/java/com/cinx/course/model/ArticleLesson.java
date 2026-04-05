package com.cinx.course.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleLesson {
    @Id
    private String lessonId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessonId")
    private Lesson lesson;

    @Column(length = 10000)
    private String content;
}
