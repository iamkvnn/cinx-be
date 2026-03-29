package com.cinx.course.mapper;

import com.cinx.course.dto.response.InstructorResponse;
import com.cinx.course.dto.response.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InstructorMapper {

    @Mapping(source = "userId", target = "id")
    InstructorResponse toResponse(UserDto userDto);
}
