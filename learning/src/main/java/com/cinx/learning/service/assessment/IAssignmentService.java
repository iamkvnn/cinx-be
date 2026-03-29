package com.cinx.learning.service.assessment;

import com.cinx.learning.dto.request.CreateAssignmentSubmissionRequest;
import com.cinx.learning.dto.response.AssignmentSubmissionResponse;
import org.springframework.data.domain.Page;

public interface IAssignmentService {
    Page<AssignmentSubmissionResponse> getAssignmentSubmissions(String assignmentId, int page, int size);
    AssignmentSubmissionResponse getAssignmentSubmission(String userId, String assignmentId);
    void submitAssignment(String userId, String assignmentId, CreateAssignmentSubmissionRequest request);
    void scoreAssignmentSubmission(String submissionId, Double score);
    void deleteAssignmentSubmission(String submissionId);
}
