package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"course_progress_id", "item_id"}))
public class LearningItemProgress extends BaseEntity {
    @Column(name = "item_id", nullable = false)
    private String itemId;
    private Boolean isCompleted;
    private Boolean isPassed;
    private Double score;

    @ManyToOne
    @JoinColumn(name = "course_progress_id", nullable = false)
    private CourseProgress courseProgress;
}
