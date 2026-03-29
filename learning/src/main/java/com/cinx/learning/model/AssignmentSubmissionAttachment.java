package com.cinx.learning.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Entity;
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
public class AssignmentSubmissionAttachment extends BaseEntity {
    private String attachmentUrl;
    private String fileKey;
    private String fileName;
    private String fileType;
    private Long fileSize;

    @ManyToOne
    private AssignmentSubmission assignmentSubmission;
}
