package com.cinx.course.service.curriculum;

import com.cinx.course.dto.response.CourseCurriculumResponse;

public interface ICurriculumService {
    CourseCurriculumResponse getPublishedCurriculum(String courseId);
    CourseCurriculumResponse getDraftCurriculum(String courseId);
}
