package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.UpdateSectionRequest;
import com.cinx.course.dto.response.SectionResponse;
import com.cinx.course.model.Section;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SectionMapper extends BaseMapper<Section, SectionResponse>, UpdateMapper<Section, UpdateSectionRequest> {
}
