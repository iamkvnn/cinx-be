package com.cinx.learning.service.assignment;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.consts.DailyGoalType;
import com.cinx.learning.dto.request.CreateAssignmentSubmissionRequest;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.AssignmentSubmissionResponse;
import com.cinx.learning.mapper.AssignmentSubmissionMapper;
import com.cinx.learning.model.AssignmentSubmission;
import com.cinx.learning.model.AssignmentSubmissionAttachment;
import com.cinx.learning.repository.AssignmentSubmissionRepository;
import com.cinx.learning.repository.AssignmentSubmissionAttachmentRepository;
import com.cinx.learning.service.authorization.LearningAuthorizationService;
import com.cinx.learning.service.dailyGoal.IDailyGoalService;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssignmentService implements IAssignmentService {
    private final AssignmentSubmissionAttachmentRepository assignmentSubmissionAttachmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final AssignmentSubmissionMapper assignmentSubmissionMapper;
    private final ILearningProgressService learningProgressService;
    private final IDailyGoalService dailyGoalService;
    private final LearningAuthorizationService authorizationService;

    @Value("${aws.s3.cdn-url}")
    private String cdnUrl;

    @Override
    public Page<AssignmentSubmissionResponse> getAssignmentSubmissions(String assignmentId, int page, int size) {
        validatePageRequest(page, size);
        return assignmentSubmissionRepository.findAllByAssignmentId(assignmentId, PageRequest.of(page - 1, size))
                .map(assignmentSubmissionMapper::toDto);
    }

    @Override
    public AssignmentSubmissionResponse getAssignmentSubmission(String userId, String assignmentId) {
        return assignmentSubmissionRepository.findByUserIdAndAssignmentId(userId, assignmentId)
                .map(assignmentSubmissionMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
    }

    @Transactional
    @Override
    public void submitAssignment(String userId, String assignmentId, CreateAssignmentSubmissionRequest request) {
        if (assignmentSubmissionRepository.findByUserIdAndAssignmentId(userId, assignmentId).isPresent()) {
            throw new BadRequestException(ErrorCode.ASSIGNMENT_ALREADY_SUBMITTED, "You have already submitted this assignment");
        }
        AssignmentSubmission assignmentSubmission = assignmentSubmissionRepository.save(
                AssignmentSubmission.builder()
                        .assignmentId(assignmentId)
                        .userId(userId)
                        .content(request.content())
                        .submissionTime(LocalDateTime.now())
                        .build()
        );

        if (request.attachments() != null && !request.attachments().isEmpty()) {
            assignmentSubmissionAttachmentRepository.saveAll(request.attachments().stream()
                    .map(file -> AssignmentSubmissionAttachment.builder()       
                            .assignmentSubmission(assignmentSubmission)
                            .fileName(file.fileName())
                            .fileType(file.fileType())
                            .fileSize(file.fileSize())
                            .attachmentUrl(cdnUrl + "/" + file.fileKey())
                            .fileKey(file.fileKey())
                            .build())
                    .toList());
        }

        dailyGoalService.recordProgress(userId, DailyGoalType.ASSIGNMENTS_SUBMITTED, 1);
    }

    @Transactional
    @Override
    public void scoreAssignmentSubmission(String submissionId, Double score) {
        AssignmentSubmission submission = authorizationService.requireAssignmentSubmissionInstructorOrAdmin(submissionId);
        if (score == null || score < 0.0 || score > 10.0) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Score must be between 0 and 10");
        }
        submission.setScore(score);
        assignmentSubmissionRepository.save(submission);
        learningProgressService.updateLearningItemProgress(
                submission.getUserId(),
                submission.getAssignmentId(),
                new UpdateLearningItemRequest(true, score >= 5.0, score)
        );
    }

    @Override
    public void deleteAssignmentSubmission(String submissionId) {
        authorizationService.requireAssignmentSubmissionDeleteAllowed(submissionId);
        assignmentSubmissionRepository.deleteById(submissionId);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 1) {
            throw new BadRequestException(ErrorCode.INVALID_PAGINATION, "page must be greater than or equal to 1");
        }
        if (size < 1) {
            throw new BadRequestException(ErrorCode.INVALID_PAGINATION, "size must be greater than or equal to 1");
        }
    }
}
