package com.cinx.course.service.lesson;

import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateLessonRequest;
import com.cinx.course.dto.request.UpdateLessonRequest;
import com.cinx.course.dto.response.LessonResponse;
import com.cinx.course.mapper.LessonMapper;
import com.cinx.course.model.Lesson;
import com.cinx.course.model.Section;
import com.cinx.course.repository.CourseRepository;
import com.cinx.course.repository.LessonRepository;
import com.cinx.course.repository.SectionRepository;
import com.cinx.course.service.change.ICourseChangeAuditService;
import com.cinx.course.messaging.CourseEventProducer;
import com.cinx.course.messaging.event.LessonChangedEvent;
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
    private final LessonMapper lessonMapper;
    private final ICourseChangeAuditService courseChangeAuditService;
    private final CourseEventProducer courseEventProducer;

    @Override
    public List<String> getLessonIdsByCourseId(String courseId) {
        return lessonRepository.findAllByCourseId(courseId);
    }

    @Override
    public List<Lesson> getLessonsBySectionId(String sectionId) {
        return lessonRepository.findAllBySectionId(sectionId);
    }

    @Override
    public Lesson getLessonById(String sectionId, String lessonId) {
        return lessonRepository.findByIdAndSectionId(lessonId, sectionId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
    }

    @Transactional
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

        List<Lesson> prerequisites = null;
        if (request.prerequisiteIds() != null && !request.prerequisiteIds().isEmpty()) {
            prerequisites = lessonRepository.findAllById(request.prerequisiteIds());
        }

        Lesson lesson = Lesson.builder()
                .title(request.title())
                .duration(request.duration())
                .orderIndex(request.orderIndex())
                .lessonType(request.lessonType())
                .isPreview(request.isPreview() != null ? request.isPreview() : false)
                .prerequisites(prerequisites)
                .section(section)
                .build();
        Lesson saved = lessonRepository.save(lesson);
        courseChangeAuditService.auditCourseItemChange(section.getCourse().getId(), saved.getId(), null, lessonMapper.toDto(saved));
        courseEventProducer.publishLessonChangedEvent(
                new LessonChangedEvent(section.getCourse().getId(), saved.getId(), "CREATED", section.getCourse().getTitle()));
        return saved;
    }

    @Transactional
    @Override
    public Lesson updateLesson(String sectionId, String lessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findByIdAndSectionId(lessonId, sectionId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
        LessonResponse oldValue = lessonMapper.toDto(lesson);
        lesson.getSection().getCourse().setStatus(CourseStatus.DRAFT);
        courseRepository.save(lesson.getSection().getCourse());
        
        lessonMapper.partialUpdate(lesson, request);
        
        if (request.prerequisiteIds() != null) {
            List<Lesson> prerequisites = lessonRepository.findAllById(request.prerequisiteIds());
            lesson.setPrerequisites(prerequisites);
        }
        
        courseChangeAuditService.auditCourseItemChange(lesson.getSection().getCourse().getId(), lesson.getId(), oldValue, lessonMapper.toDto(lesson));
        Lesson updated = lessonRepository.save(lesson);
        courseEventProducer.publishLessonChangedEvent(
                new LessonChangedEvent(lesson.getSection().getCourse().getId(), lesson.getId(), "UPDATED", lesson.getSection().getCourse().getTitle()));
        return updated;
    }

    @Transactional
    @Override
    public void deleteLesson(String sectionId, String lessonId) {
        Lesson lesson = lessonRepository.findByIdAndSectionId(lessonId, sectionId)
                .orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId));
        lesson.getSection().getCourse().setStatus(CourseStatus.DRAFT);
        courseRepository.save(lesson.getSection().getCourse());
        courseChangeAuditService.auditCourseItemChange(lesson.getSection().getCourse().getId(), lesson.getId(), lessonMapper.toDto(lesson), null);
        lessonRepository.delete(lesson);
        courseEventProducer.publishLessonChangedEvent(
                new LessonChangedEvent(lesson.getSection().getCourse().getId(), lesson.getId(), "DELETED", lesson.getSection().getCourse().getTitle()));
    }
}
