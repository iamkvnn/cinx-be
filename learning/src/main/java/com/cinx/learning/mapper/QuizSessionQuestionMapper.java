package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.learning.dto.response.QuizSessionQuestionResponse;
import com.cinx.learning.model.QuizSessionQuestion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuizSessionQuestionMapper extends BaseMapper<QuizSessionQuestion, QuizSessionQuestionResponse> {
}
