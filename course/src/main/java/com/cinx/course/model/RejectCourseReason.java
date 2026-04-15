package com.cinx.course.model;

import com.cinx.common.model.AuditableEntity;
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
public class RejectCourseReason extends AuditableEntity {
    private String courseId;
    private String reason;
}
