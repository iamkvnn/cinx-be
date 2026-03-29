package com.cinx.course.model;

import com.cinx.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Section extends AuditableEntity {

    private String title;
    private String description;
    private Long duration;
    private Integer orderIndex;

    @ManyToOne
    private Course course;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    private List<Lesson> lessons = new ArrayList<>();
}
