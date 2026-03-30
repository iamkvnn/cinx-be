package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class LearningPathItem extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name = "learning_path_id")
    private UserLearningPath learningPath;

    private String courseId;
    private String lessonId;
    
    private Integer orderIndex;
    private Boolean isSuggested;
    private Boolean isCompleted;
}
