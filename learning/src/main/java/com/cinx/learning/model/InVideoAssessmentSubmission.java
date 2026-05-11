package com.cinx.learning.model;

import jakarta.persistence.Entity;
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
public class InVideoAssessmentSubmission extends UserSubmission {
    private String videoLessonId;
    private String videoAssessmentId;
    private String userAnswer;
}
