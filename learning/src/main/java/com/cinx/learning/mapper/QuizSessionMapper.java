package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.learning.dto.response.QuizSessionResponse;
import com.cinx.learning.model.QuizSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuizSessionMapper extends BaseMapper<QuizSession, QuizSessionResponse> {
}
