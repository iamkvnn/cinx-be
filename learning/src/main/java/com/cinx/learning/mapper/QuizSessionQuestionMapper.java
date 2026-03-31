package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.learning.dto.response.QuizSessionQuestionResponse;
import com.cinx.learning.model.QuizSessionQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizSessionQuestionMapper extends BaseMapper<QuizSessionQuestion, QuizSessionQuestionResponse> {
    @Override
    @Mapping(target = "isCorrect", expression = "java(entity.getCorrectAnswer() != null && entity.getCorrectAnswer().equals(entity.getUserAnswer()))")
    QuizSessionQuestionResponse toDto(QuizSessionQuestion entity);
}
