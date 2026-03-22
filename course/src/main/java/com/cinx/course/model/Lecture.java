package com.cinx.course.model;

import com.cinx.course.consts.LectureType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;
    private String contentUrl;
    private Long duration;
    private Integer orderIndex;
    private LectureType lectureType;

    @ManyToOne
    private Section section;
}
