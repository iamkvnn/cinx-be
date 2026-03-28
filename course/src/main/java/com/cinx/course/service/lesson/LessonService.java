package com.cinx.course.service.lesson;

import com.cinx.course.model.Lesson;
import com.cinx.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService implements ILessonService {
    private final LessonRepository lessonRepository;

    @Override
    public List<Lesson> getLessonsBySectionIds(List<String> sectionIds) {
        return lessonRepository.findAllBySectionIdIn(sectionIds);
    }

    @Override
    public void updateLesson(Lesson lesson) {
        lessonRepository.save(lesson);
    }
}
