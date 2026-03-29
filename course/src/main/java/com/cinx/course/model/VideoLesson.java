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
    
    // AWS S3 related fields
    private String videoUrl;
    private String s3ObjectKey;
    private String fileName;
    private String fileType;
    private Long fileSize;
    
    // Additional video info
    private Integer duration; // in seconds
    private String status; // e.g., PROCESSING, READY, FAILED

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessonId")
    private Lesson lesson;
}
