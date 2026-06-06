package com.cinx.course.service.assignment;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.course.consts.LessonType;
import com.cinx.course.dto.request.CreateAssignmentLessonRequest;
import com.cinx.course.dto.request.UpdateAssignmentLessonRequest;
import com.cinx.course.dto.response.AssignmentLessonResponse;
import com.cinx.course.mapper.AssignmentMapper;
import com.cinx.course.repository.AssignmentAttachmentRepository;
import com.cinx.course.repository.AssignmentLessonRepository;
import com.cinx.course.service.lesson.ILessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssignmentService implements IAssignmentService {
    private final AssignmentLessonRepository assignmentLessonRepository;
    private final AssignmentAttachmentRepository assignmentAttachmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final ILessonService lessonService;

    @Value("${aws.s3.cdn-url}")
    private String cdnUrl;

    @Override
    public AssignmentLessonResponse getAssignmentByLessonId(String courseId, String lessonId) {
        lessonService.ensureLessonBelongsToCourse(courseId, lessonId, LessonType.ASSIGNMENT);
        return assignmentLessonRepository.findByLessonId(lessonId)
                .map(assignmentMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Assignment not found for lessonId: " + lessonId));
    }

    @Override
    public void createAssignment(String courseId, String lessonId, CreateAssignmentLessonRequest request) {
        lessonService.ensureLessonBelongsToCourse(courseId, lessonId, LessonType.ASSIGNMENT);
        assignmentLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            throw new AlreadyExistException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Assignment already exists for lessonId: " + lessonId);
        },() -> {
            var assignmentLesson = assignmentMapper.toModel(request);
            assignmentLesson.setLessonId(lessonId);
            if (assignmentLesson.getAttachments() != null) {
                assignmentLesson.getAttachments().forEach(attachment -> {
                    attachment.setAttachmentUrl(cdnUrl + "/" + attachment.getFileKey());
                    attachment.setAssignmentLesson(assignmentLesson);
                });
            }
            assignmentLessonRepository.save(assignmentLesson);
        });
    }

    @Override
    public void updateAssignment(String courseId, String lessonId, UpdateAssignmentLessonRequest request) {
        lessonService.ensureLessonBelongsToCourse(courseId, lessonId, LessonType.ASSIGNMENT);
        assignmentLessonRepository.findByLessonId(lessonId).ifPresentOrElse(existing -> {
            assignmentMapper.partialUpdate(existing, request);
            if (existing.getAttachments() != null && !existing.getAttachments().isEmpty()) {
                assignmentAttachmentRepository.deleteAll(existing.getAttachments());
            }
            assignmentLessonRepository.save(existing);
            if (existing.getAttachments() != null) {
                assignmentAttachmentRepository.saveAll(existing.getAttachments().stream().peek(attachment -> {
                    attachment.setAttachmentUrl(cdnUrl + "/" + attachment.getFileKey());
                    attachment.setAssignmentLesson(existing);
                }).toList());
            }
        },() -> {
            throw new NotFoundException("Assignment not found for lessonId: " + lessonId);
        });
    }
}
