package com.cinx.course.mapper;

import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.model.Lesson;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

import com.cinx.common.mapper.BaseMapper;

@Mapper(componentModel = "spring")
public interface LessonMapper extends
        BaseMapper<Lesson, LessonResponse>,
        CreateMapper<Lesson, CreateLessonRequest>,
        UpdateMapper<Lesson, UpdateLessonRequest> {

    @Mapping(target = "prerequisiteIds", source = "prerequisites", qualifiedByName = "lessonsToIds")
    LessonResponse toDto(Lesson e);

    @Mapping(target = "prerequisites", ignore = true)
    Lesson toModel(CreateLessonRequest request);

    @Mapping(target = "prerequisites", ignore = true)
    void partialUpdate(@org.mapstruct.MappingTarget Lesson entity, UpdateLessonRequest dto);

    @Named("lessonsToIds")
    default List<String> lessonsToIds(List<Lesson> lessons) {
        if (lessons == null) return null;
        return lessons.stream().map(Lesson::getId).collect(Collectors.toList());
    }
}
