package com.cinx.course.service.lesson;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.model.Lesson;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService implements ILessonService {
    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<Lesson> getLessonsBySectionId(String sectionId) {
        return lessonRepository.findAllBySectionId(sectionId);
    }

    @Override
    public Lesson getLessonById(String sectionId, String lessonId) {
        return lessonRepository.findByIdAndSectionId(lessonId, sectionId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
    }

    @Override
    public Lesson getForUpdate(String lessonId, LessonType lessonType) {
        Lesson lesson = lessonRepository.findByIdAndLessonType(lessonId, lessonType)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
        lesson.getSection().getCourse().setStatus(CourseStatus.DRAFT);
        courseRepository.save(lesson.getSection().getCourse());
        return lesson;
    }

    @Transactional
    @Override
    public Lesson createLesson(String sectionId, CreateLessonRequest request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NotFoundException("Section not found with id: " + sectionId));
        section.getCourse().setStatus(CourseStatus.DRAFT);
        courseRepository.save(section.getCourse());

        Lesson lesson = Lesson.builder()
                .title(request.title())
                .duration(request.duration())
                .orderIndex(request.orderIndex())
                .lessonType(request.lessonType())
                .section(section)
                .build();
        return lessonRepository.save(lesson);
    }

    @Transactional
    @Override
    public Lesson updateLesson(String sectionId, String lessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findByIdAndSectionId(lessonId, sectionId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
        lesson.getSection().getCourse().setStatus(CourseStatus.DRAFT);
        courseRepository.save(lesson.getSection().getCourse());

        if (request.title() != null) {
            lesson.setTitle(request.title());
        }
        if (request.duration() != null) {
            lesson.setDuration(request.duration());
        }
        if (request.orderIndex() != null) {
            lesson.setOrderIndex(request.orderIndex());
        }

        return lessonRepository.save(lesson);
    }

    @Transactional
    @Override
    public void deleteLesson(String sectionId, String lessonId) {
        Lesson lesson = lessonRepository.findByIdAndSectionId(lessonId, sectionId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
        lesson.getSection().getCourse().setStatus(CourseStatus.DRAFT);
        courseRepository.save(lesson.getSection().getCourse());
        lessonRepository.delete(lesson);
    }
}
