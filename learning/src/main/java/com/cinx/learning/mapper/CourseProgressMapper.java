package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.learning.dto.request.UpdateCourseProgressRequest;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.model.CourseProgress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseProgressMapper extends
        BaseMapper<CourseProgress, CourseProgressResponse>,
        UpdateMapper<CourseProgress, UpdateCourseProgressRequest> {
}
