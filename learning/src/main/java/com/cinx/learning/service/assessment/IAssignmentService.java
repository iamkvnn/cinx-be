package com.cinx.learning.service.assessment;

import com.cinx.learning.dto.request.CreateAssignmentSubmissionRequest;
import com.cinx.learning.dto.response.AssignmentSubmissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IAssignmentService {
    Page<AssignmentSubmissionResponse> getAssignmentSubmissions(String assignmentId, int page, int size);
    AssignmentSubmissionResponse getAssignmentSubmission(String userId, String assignmentId);
    void submitAssignment(String userId, String assignmentId, CreateAssignmentSubmissionRequest request, List<MultipartFile> attachments);
    void scoreAssignmentSubmission(String submissionId, Double score);
    void deleteAssignmentSubmission(String submissionId);
}
