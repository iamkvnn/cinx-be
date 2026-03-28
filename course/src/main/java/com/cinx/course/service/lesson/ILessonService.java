package com.cinx.course.service.lesson;

import com.cinx.course.model.Lesson;

import java.util.List;

public interface ILessonService {
    List<Lesson> getLessonsBySectionIds(List<String> sectionIds);
    void updateLesson(Lesson lesson);
}
