package com.cinx.learning.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LearningItemProgress {
    @Id
    private String itemId;
    private Boolean isCompleted;
    private Boolean isPassed;
    private Double score;

    @ManyToOne
    private CourseProgress courseProgress;
}
