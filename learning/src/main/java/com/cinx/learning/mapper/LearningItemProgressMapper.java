package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.LearningItemProgressResponse;
import com.cinx.learning.model.LearningItemProgress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LearningItemProgressMapper extends
        BaseMapper<LearningItemProgress, LearningItemProgressResponse>,
        UpdateMapper<LearningItemProgress, UpdateLearningItemRequest> {
}
