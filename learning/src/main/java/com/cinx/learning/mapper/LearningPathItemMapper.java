package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.learning.dto.response.LearningPathItemResponse;
import com.cinx.learning.model.LearningPathItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LearningPathItemMapper extends BaseMapper<LearningPathItem, LearningPathItemResponse> {
}
