package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.course.dto.response.CourseChangeResponse;
import com.cinx.course.model.CourseChange;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseChangeMapper extends BaseMapper<CourseChange, CourseChangeResponse> {
}
