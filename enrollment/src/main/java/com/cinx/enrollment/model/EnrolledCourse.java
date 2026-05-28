package com.cinx.enrollment.model;

import com.cinx.common.model.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "user_id"}))
public class EnrolledCourse extends AuditableEntity {
    private String courseId;
    private String userId;
}
