package com.cinx.learning.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmission extends UserSubmission {
    private String assignmentId;
    private String content;
    private Double score;
    private String feedback;

    @OneToMany(mappedBy = "", fetch = FetchType.EAGER)
    private List<AssignmentSubmissionAttachment> attachments;
}
