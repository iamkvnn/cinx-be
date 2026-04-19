package com.cinx.enrollment.model;

import com.cinx.common.model.AuditableEntity;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EnrolledCourse extends AuditableEntity {
    private String courseId;
    private String userId;
}
