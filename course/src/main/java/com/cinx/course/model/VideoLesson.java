package com.cinx.course.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToMany(mappedBy = "videoLesson", fetch = FetchType.LAZY)
    private List<VideoQuestion> questions;
}
