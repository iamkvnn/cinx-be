package com.cinx.course.service.assignment;

import com.cinx.course.dto.request.CreateAssignmentLessonRequest;
import com.cinx.course.dto.response.AssignmentLessonResponse;

public interface IAssignmentService {
    AssignmentLessonResponse getAssignmentByLessonId(String lessonId);
    void createAssignment(String lessonId, CreateAssignmentLessonRequest request);
    void updateAssignment(String lessonId, CreateAssignmentLessonRequest request);
    void deleteAssignment(String lessonId);
}
