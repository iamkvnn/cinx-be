package com.cinx.course.service.assignment;

import com.cinx.course.dto.request.CreateAssignmentLessonRequest;
import com.cinx.course.dto.request.UpdateAssignmentLessonRequest;
import com.cinx.course.dto.response.AssignmentLessonResponse;

public interface IAssignmentService {
    AssignmentLessonResponse getAssignmentByLessonId(String courseId, String lessonId);
    void createAssignment(String courseId, String lessonId, CreateAssignmentLessonRequest request);
    void updateAssignment(String courseId, String lessonId, UpdateAssignmentLessonRequest request);
}
