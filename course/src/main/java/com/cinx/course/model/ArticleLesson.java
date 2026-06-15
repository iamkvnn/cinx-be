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

    private String articleUrl;
    private String fileKey;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
