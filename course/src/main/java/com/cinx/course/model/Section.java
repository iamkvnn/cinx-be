package com.cinx.course.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;
    private String description;
    private Long duration;
    private Integer orderIndex;

    @ManyToOne
    private Course course;

    @OneToMany
    private List<Lecture> lectures = new ArrayList<>();
}
