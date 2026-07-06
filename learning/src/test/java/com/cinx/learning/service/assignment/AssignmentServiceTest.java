package com.cinx.learning.service.assignment;

import com.cinx.learning.mapper.AssignmentSubmissionMapper;
import com.cinx.learning.repository.AssignmentSubmissionAttachmentRepository;
import com.cinx.learning.repository.AssignmentSubmissionRepository;
import com.cinx.learning.service.authorization.LearningAuthorizationService;
import com.cinx.learning.service.dailyGoal.IDailyGoalService;
import com.cinx.learning.service.learningProgress.ILearningProgressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {
    @Mock
    private AssignmentSubmissionAttachmentRepository assignmentSubmissionAttachmentRepository;
    @Mock
    private AssignmentSubmissionRepository assignmentSubmissionRepository;
    @Mock
    private AssignmentSubmissionMapper assignmentSubmissionMapper;
    @Mock
    private ILearningProgressService learningProgressService;
    @Mock
    private IDailyGoalService dailyGoalService;
    @Mock
    private LearningAuthorizationService authorizationService;

    @InjectMocks
    private AssignmentService assignmentService;

    @Test
    void getAssignmentSubmissionsUsesSortParameter() {
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(assignmentSubmissionRepository.findAllByAssignmentId(eq("assignment-1"), pageableCaptor.capture()))
                .thenReturn(Page.empty());

        assignmentService.getAssignmentSubmissions("assignment-1", 1, 10, "{\"submissionTime\":\"ASC\"}");

        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("submissionTime");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}
