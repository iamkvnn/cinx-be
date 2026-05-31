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

    @Column(length = 10000)
    private String content;
}
