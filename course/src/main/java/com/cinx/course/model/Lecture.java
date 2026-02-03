package com.cinx.course.model;

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

    @ManyToOne
    private Section section;
}
