package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.messaging.event.CourseEvent;
import com.cinx.course.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseMapper extends BaseMapper<Course, CourseResponse>,
        CreateMapper<Course, CreateCourseRequest>,
        UpdateMapper<Course, UpdateCourseRequest> {
    @Override
    @Mapping(source = "category.name", target = "category")
    CourseResponse toDto(Course entity);

    @Override
    @Mapping(target = "sections", ignore = true)
    void partialUpdate(@MappingTarget Course entity, UpdateCourseRequest request);

    @Mapping(source = "category.name", target = "category")
    CourseDetailResponse toDetailDto(Course entity);
}
