package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import com.cinx.learning.consts.LearningPathStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserLearningPath extends BaseEntity {
    private String userId;
    private String title;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private LearningPathStatus status;
    
    private Double currentProgress;
    private Integer totalItems;
    private Integer completedItems;

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LearningPathItem> items = new ArrayList<>();
}
