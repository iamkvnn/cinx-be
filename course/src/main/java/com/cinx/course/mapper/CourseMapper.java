package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseAggregate;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.messaging.event.CourseEvent;
import com.cinx.course.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {
                InstructorMapper.class,
                CourseImageMapper.class,
                SectionMapper.class
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CourseMapper extends BaseMapper<Course, CourseResponse>,
        CreateMapper<Course, CreateCourseRequest>,
        UpdateMapper<Course, UpdateCourseRequest> {
    @Override
    @Mapping(source = "category.name", target = "category")
    CourseResponse toDto(Course entity);


    @Mapping(target = ".", source = "course")
    @Mapping(target = "category", source = "course.category.name")
    @Mapping(target = "instructor", source = "instructor")
    CourseResponse toDto(CourseAggregate aggregate);

    @Mapping(target = ".", source = "course")
    @Mapping(target = "category", source = "course.category.name")
    @Mapping(target = "instructor", source = "instructor")
    CourseDetailResponse toDetailDto(CourseAggregate aggregate);

    @Override
    @Mapping(target = "sections", ignore = true)
    void partialUpdate(@MappingTarget Course entity, UpdateCourseRequest request);
}
