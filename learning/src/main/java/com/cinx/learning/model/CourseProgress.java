package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgress extends BaseEntity {
    private String userId;
    private String courseId;
    private Boolean isCompleted;
    private Boolean isPassed;
    private Double avgScore;
    private Integer totalItems;
    private Integer completedItems;
    private LocalDateTime completionTime;

    @OneToMany(mappedBy = "courseProgress")
    private List<LearningItemProgress> learningItemProgressList;
}
