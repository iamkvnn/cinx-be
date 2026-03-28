package com.cinx.course.service.assignment;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.dto.request.CreateAssignmentLessonRequest;
import com.cinx.course.dto.response.AssignmentLessonResponse;
import com.cinx.course.mapper.AssignmentMapper;
import com.cinx.course.repository.AssignmentLessonRepository;
import com.cinx.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssignmentService implements IAssignmentService {
    private final AssignmentLessonRepository assignmentLessonRepository;
    private final AssignmentMapper assignmentMapper;
    private final LessonRepository lessonRepository;

    @Override
    public AssignmentLessonResponse getAssignmentByLessonId(String lessonId) {
        return assignmentLessonRepository.findByLessonId(lessonId)
                .map(assignmentMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Assignment not found for lessonId: " + lessonId));
    }

    @Override
    public void createAssignment(String lessonId, CreateAssignmentLessonRequest request) {
            assignmentLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
                throw new AlreadyExistException("Assignment already exists for lessonId: " + lessonId);
            },() -> {
                var assignmentLesson = assignmentMapper.toModel(request);
                assignmentLesson.setLesson(lessonRepository.findById(lessonId).orElseThrow(() -> new NotFoundException("Lesson not found with id: " + lessonId)));
                assignmentLessonRepository.save(assignmentLesson);
            });
    }

    @Override
    public void updateAssignment(String lessonId, CreateAssignmentLessonRequest request) {
            assignmentLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
                assignmentMapper.partialUpdate(existing, request);
                assignmentLessonRepository.save(existing);
            },() -> {
                throw new NotFoundException("Assignment not found for lessonId: " + lessonId);
            });
    }

    @Override
    public void deleteAssignment(String lessonId) {
        assignmentLessonRepository.findByLessonId(lessonId)
                .ifPresentOrElse(assignmentLessonRepository::delete, () -> {
                    throw new NotFoundException("Assignment not found for lessonId: " + lessonId);
        });
    }
}
