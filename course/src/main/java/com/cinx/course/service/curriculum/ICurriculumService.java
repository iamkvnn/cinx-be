package com.cinx.course.service.curriculum;

import com.cinx.course.dto.response.CourseCurriculumResponse;

public interface ICurriculumService {
    CourseCurriculumResponse getPublishedCurriculum(String courseId);
    CourseCurriculumResponse getPublishedSnapshotCurriculum(String courseId);
    CourseCurriculumResponse getOwnedPublishedSnapshotCurriculum(String courseId);
    CourseCurriculumResponse getEnrolledCurriculum(String courseId);
    CourseCurriculumResponse getDraftCurriculum(String courseId);
    CourseCurriculumResponse getOwnedDraftCurriculum(String courseId);
}
