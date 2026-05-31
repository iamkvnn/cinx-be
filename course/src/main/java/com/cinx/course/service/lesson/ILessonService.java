package com.cinx.course.service.lesson;

import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.ReorderLessonsRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.dto.response.SectionLessonsOrderResponse;
import com.cinx.course.model.Lesson;

import java.util.List;

public interface ILessonService {
    List<String> getLessonIdsByCourseId(String courseId);
    Lesson getForUpdate(String courseId, String sectionId, String lessonId, LessonType lessonType);
    LessonResponse createLesson(String courseId, String sectionId, CreateLessonRequest request);
    LessonResponse updateLesson(String courseId, String sectionId, String lessonId, UpdateLessonRequest request);
    List<SectionLessonsOrderResponse> reorderLessons(String courseId, ReorderLessonsRequest request);
    void deleteLesson(String courseId, String sectionId, String lessonId);
    boolean isLessonInstructor(String lessonStableId, String userId);
}
