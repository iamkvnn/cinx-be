package com.cinx.enrollment.model;

import com.cinx.common.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EnrolledCourse extends BaseEntity {
    private String courseId;
    private String userId;
}
