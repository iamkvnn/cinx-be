package com.cinx.course.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.course.dto.request.CreateAssignmentLessonRequest;
import com.cinx.course.dto.request.UpdateAssignmentLessonRequest;
import com.cinx.course.dto.response.AssignmentLessonResponse;
import com.cinx.course.model.AssignmentLesson;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssignmentMapper extends BaseMapper<AssignmentLesson, AssignmentLessonResponse>,
        CreateMapper<AssignmentLesson, CreateAssignmentLessonRequest>,
        UpdateMapper<AssignmentLesson, UpdateAssignmentLessonRequest> {
}
