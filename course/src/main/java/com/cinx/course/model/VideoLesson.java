package com.cinx.course.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLesson {
    @Id
    private String lessonId;

    private String videoUrl;
    private String fileKey;
    private String fileName;
    private String fileType;
    private Long fileSize;

    private Integer duration;
    private String status;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessonId")
    private Lesson lesson;
}
