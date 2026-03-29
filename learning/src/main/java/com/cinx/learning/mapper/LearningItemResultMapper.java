package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.learning.dto.response.LearningItemResultResponse;
import com.cinx.learning.model.LearningItemResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LearningItemResultMapper extends BaseMapper<LearningItemResult, LearningItemResultResponse> {
}
