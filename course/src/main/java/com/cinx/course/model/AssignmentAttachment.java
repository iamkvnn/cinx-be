package com.cinx.course.model;

import com.cinx.common.model.BaseEntity;
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
public class AssignmentAttachment extends BaseEntity {
    private String attachmentUrl;
    private String s3ObjectKey;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
