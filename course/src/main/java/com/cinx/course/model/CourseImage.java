package com.cinx.course.model;

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
public class CourseImage extends BaseEntity {
    private String imageUrl;
}
