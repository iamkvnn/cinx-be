package com.cinx.course.model;

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
public class AssignmentAttachment extends BaseEntity {
    private String attachmentUrl;
    private String fileKey;
    private String fileName;
    private String fileType;
    private Long fileSize;

    @ManyToOne
    @JoinColumn(name = "assignment_lesson_id")
    private AssignmentLesson assignmentLesson;
}
