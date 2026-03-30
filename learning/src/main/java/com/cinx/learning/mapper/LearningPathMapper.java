package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.learning.dto.response.LearningPathResponse;
import com.cinx.learning.model.UserLearningPath;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LearningPathMapper extends BaseMapper<UserLearningPath, LearningPathResponse> {
}
