package com.cinx.learning.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResult {
    @Id
    private String submissionId;
    private Double score;
    private String feedback;
}
