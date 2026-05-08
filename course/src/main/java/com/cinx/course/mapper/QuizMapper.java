package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateQuizLessonRequest;
import com.cinx.course.dto.request.UpdateQuizLessonRequest;
import com.cinx.course.dto.response.QuizLessonResponse;
import com.cinx.course.model.QuizLesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizMapper extends BaseMapper<QuizLesson, QuizLessonResponse>,
        CreateMapper<QuizLesson, CreateQuizLessonRequest>,
        UpdateMapper<QuizLesson, UpdateQuizLessonRequest> {

    @Override
    @Mapping(target = "lessonId", source = "lessonId")
    QuizLessonResponse toDto(QuizLesson quizLesson);
}
