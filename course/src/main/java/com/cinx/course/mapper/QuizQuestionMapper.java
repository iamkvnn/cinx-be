package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateQuizQuestionRequest;
import com.cinx.course.dto.request.UpdateQuizQuestionRequest;
import com.cinx.course.dto.response.QuizQuestionResponse;
import com.cinx.course.model.QuizQuestion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuizQuestionMapper extends
        BaseMapper<QuizQuestion, QuizQuestionResponse>,
        CreateMapper<QuizQuestion, CreateQuizQuestionRequest>,
        UpdateMapper<QuizQuestion, UpdateQuizQuestionRequest> {
}
