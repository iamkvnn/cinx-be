package com.cinx.course.service.lesson;

import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.MoveLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonPositionResponse;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.model.Lesson;

import java.util.List;

public interface ILessonService {
    List<String> getLessonIdsByCourseId(String courseId);
    List<String> getEnrolledLessonIdsByCourseId(String courseId);
    Lesson getForUpdate(String courseId, String sectionId, String lessonId, LessonType lessonType);
    void ensureLessonBelongsToCourse(String courseId, String lessonId, LessonType lessonType);
    LessonResponse createLesson(String courseId, String sectionId, CreateLessonRequest request);
    LessonResponse updateLesson(String courseId, String sectionId, String lessonId, UpdateLessonRequest request);
    LessonPositionResponse moveLesson(String courseId, String lessonId, MoveLessonRequest request);
    void deleteLesson(String courseId, String sectionId, String lessonId);
    boolean isLessonInstructor(String lessonStableId, String userId);
}
