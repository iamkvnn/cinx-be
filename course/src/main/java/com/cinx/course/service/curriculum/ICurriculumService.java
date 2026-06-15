package com.cinx.course.service.curriculum;

import com.cinx.course.dto.response.CourseCurriculumResponse;

public interface ICurriculumService {
    CourseCurriculumResponse getReadableCurriculum(String currentUserId, String courseId);
    CourseCurriculumResponse getEnrolledCurriculum(String courseId);
    CourseCurriculumResponse getEditableDraftCurriculum(String currentUserId, String courseId);
}
