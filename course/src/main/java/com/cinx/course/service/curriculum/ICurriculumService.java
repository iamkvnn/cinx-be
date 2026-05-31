package com.cinx.course.service.curriculum;

import com.cinx.course.dto.request.ReorderLessonsRequest;
import com.cinx.course.dto.response.CourseCurriculumResponse;

public interface ICurriculumService {
    CourseCurriculumResponse getPublishedCurriculum(String courseId);
    CourseCurriculumResponse getDraftCurriculum(String courseId);
    CourseCurriculumResponse reorderCurriculum(String courseId, ReorderLessonsRequest request);
}
