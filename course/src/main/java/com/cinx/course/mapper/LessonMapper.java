package com.cinx.course.mapper;

import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.model.Lesson;

import org.mapstruct.Mapper;

import com.cinx.common.mapper.BaseMapper;

@Mapper(componentModel = "spring")
public interface LessonMapper extends
        BaseMapper<Lesson, LessonResponse>,
        CreateMapper<Lesson, CreateLessonRequest>,
        UpdateMapper<Lesson, UpdateLessonRequest> {
}
