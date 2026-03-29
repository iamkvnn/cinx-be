package com.cinx.course.mapper;

import com.cinx.course.dto.response.CourseImageResponse;
import com.cinx.course.model.CourseImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseImageMapper {
    CourseImageResponse toResponse(CourseImage image);
}
