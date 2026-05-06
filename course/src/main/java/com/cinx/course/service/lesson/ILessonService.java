package com.cinx.course.service.lesson;

import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.model.Lesson;

import java.util.List;

public interface ILessonService {
    List<String> getLessonIdsByCourseId(String courseId);
    List<Lesson> getLessonsBySectionId(String sectionId);
    Lesson getLessonById(String sectionId, String lessonId);
    Lesson getForUpdate(String lessonId, LessonType lessonType);
    Lesson createLesson(String sectionId, CreateLessonRequest request);
    Lesson updateLesson(String sectionId, String lessonId, UpdateLessonRequest request);
    void deleteLesson(String sectionId, String lessonId);
}
