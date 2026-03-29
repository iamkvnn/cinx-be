package com.cinx.course.mapper;

import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.model.Section;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SectionMapper {
    SectionResponse toResponse(Section section);
}
