package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.learning.dto.response.AssignmentSubmissionResponse;
import com.cinx.learning.model.AssignmentSubmission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssignmentSubmissionMapper extends
        BaseMapper<AssignmentSubmission, AssignmentSubmissionResponse> {
}
