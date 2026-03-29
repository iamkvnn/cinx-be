package com.cinx.learning.service.assessment;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.learning.dto.request.CreateAssignmentSubmissionRequest;
import com.cinx.learning.dto.request.UpdateLearningItemRequest;
import com.cinx.learning.dto.response.AssignmentSubmissionResponse;
import com.cinx.learning.mapper.AssignmentSubmissionMapper;
import com.cinx.learning.model.AssignmentSubmission;
import com.cinx.learning.model.AssignmentSubmissionAttachment;
import com.cinx.learning.repository.AssignmentSubmissionRepository;
import com.cinx.learning.repository.AssignmentSubmissionAttachmentRepository;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService implements IAssignmentService {
    private final AssignmentSubmissionAttachmentRepository assignmentSubmissionAttachmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final AssignmentSubmissionMapper assignmentSubmissionMapper;
    private final ILearningProgressService learningProgressService;

    @Value("${aws.s3.cdn-url}")
    private String cdnUrl;

    @Override
    public Page<AssignmentSubmissionResponse> getAssignmentSubmissions(String assignmentId, int page, int size) {
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
            throw new BadRequestException("You have already submitted this assignment");
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

        learningProgressService.updateLearningItemProgress(
                userId,
                assignmentId,
                new UpdateLearningItemRequest(true, null, null)
        );
    }

    @Transactional
    @Override
    public void scoreAssignmentSubmission(String submissionId, Double score) {
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
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
        if (!assignmentSubmissionRepository.existsById(submissionId)) {
            throw new NotFoundException("Submission not found");
        }
        assignmentSubmissionRepository.deleteById(submissionId);
    }
}
