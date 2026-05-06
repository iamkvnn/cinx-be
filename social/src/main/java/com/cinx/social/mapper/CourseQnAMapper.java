package com.cinx.social.mapper;

import com.cinx.social.dto.request.CreateQuestionRequest;
import com.cinx.social.dto.request.UpdateQuestionRequest;
import com.cinx.social.dto.request.CreateAnswerRequest;
import com.cinx.social.dto.request.UpdateAnswerRequest;
import com.cinx.social.dto.response.QuestionDto;
import com.cinx.social.dto.response.AnswerDto;
import com.cinx.social.model.CourseQuestion;
import com.cinx.social.model.CourseAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseQnAMapper {
    QuestionDto toDto(CourseQuestion entity);
    CourseQuestion toModel(CreateQuestionRequest request);
    void partialUpdate(@MappingTarget CourseQuestion entity, UpdateQuestionRequest dto);

    AnswerDto toDto(CourseAnswer entity);
    CourseAnswer toModel(CreateAnswerRequest request);
    void partialUpdate(@MappingTarget CourseAnswer entity, UpdateAnswerRequest dto);
}
