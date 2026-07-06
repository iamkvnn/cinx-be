package com.cinx.social.service.admin;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.client.UserClient;
import com.cinx.social.dto.response.AdminReportResponse;
import com.cinx.social.dto.response.UserSummaryResponse;
import com.cinx.social.model.CourseAnswer;
import com.cinx.social.model.CourseQuestion;
import com.cinx.social.model.Report;
import com.cinx.social.model.ReportType;
import com.cinx.social.model.Review;
import com.cinx.social.repository.CourseAnswerRepository;
import com.cinx.social.repository.CourseQuestionRepository;
import com.cinx.social.repository.ReportRepository;
import com.cinx.social.repository.ReviewRepository;
import com.cinx.social.service.ICourseQnAService;
import com.cinx.social.service.review.IReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private CourseQuestionRepository questionRepository;
    @Mock
    private CourseAnswerRepository answerRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private IReviewService reviewService;
    @Mock
    private ICourseQnAService qnaService;

    @InjectMocks
    private AdminReportService adminReportService;

    @Test
    void getReportsReturnsReviewContentWithReporterAndOwner() {
        Report report = report("report-1", "reporter-1", "review-1", ReportType.REVIEW);
        Review review = Review.builder()
                .userId("owner-1")
                .courseId("course-1")
                .content("Reported review")
                .rating(4.5)
                .createdAt(LocalDateTime.parse("2025-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2025-01-01T11:00:00"))
                .build();
        review.setId("review-1");

        when(reportRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(report)));
        when(reviewRepository.findAllById(List.of("review-1"))).thenReturn(List.of(review));
        when(userClient.getUsersByIds(List.of("reporter-1", "owner-1"))).thenReturn(new ApiResponse<>(true, "ok", List.of(
                user("reporter-1", "Reporter"),
                user("owner-1", "Owner")
        )));

        Page<AdminReportResponse> result = adminReportService.getReports(null, 1, 10, null);

        AdminReportResponse response = result.getContent().get(0);
        assertThat(response.reporter().name()).isEqualTo("Reporter");
        assertThat(response.reportedContent().owner().name()).isEqualTo("Owner");
        assertThat(response.reportedContent().content()).isEqualTo("Reported review");
        assertThat(response.reportedContent().rating()).isEqualTo(4.5);
        assertThat(response.reportedContent().courseId()).isEqualTo("course-1");
    }

    @Test
    void getReportsReturnsQuestionContent() {
        Report report = report("report-1", "reporter-1", "question-1", ReportType.QUESTION);
        CourseQuestion question = CourseQuestion.builder()
                .userId("owner-1")
                .courseId("course-1")
                .lessonId("lesson-1")
                .title("Question title")
                .content("Question content")
                .build();
        question.setId("question-1");

        when(reportRepository.findByType(eq(ReportType.QUESTION), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(report)));
        when(questionRepository.findAllById(List.of("question-1"))).thenReturn(List.of(question));
        when(userClient.getUsersByIds(List.of("reporter-1", "owner-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(user("reporter-1", "Reporter"), user("owner-1", "Owner"))));

        AdminReportResponse response = adminReportService.getReports(ReportType.QUESTION, 1, 10, null).getContent().get(0);

        assertThat(response.reportedContent().title()).isEqualTo("Question title");
        assertThat(response.reportedContent().content()).isEqualTo("Question content");
        assertThat(response.reportedContent().courseId()).isEqualTo("course-1");
        assertThat(response.reportedContent().lessonId()).isEqualTo("lesson-1");
        verify(reportRepository).findByType(eq(ReportType.QUESTION), any(Pageable.class));
    }

    @Test
    void getReportsReturnsAnswerContent() {
        Report report = report("report-1", "reporter-1", "answer-1", ReportType.ANSWER);
        CourseAnswer answer = CourseAnswer.builder()
                .userId("owner-1")
                .questionId("question-1")
                .content("Answer content")
                .build();
        answer.setId("answer-1");

        when(reportRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(report)));
        when(answerRepository.findAllById(List.of("answer-1"))).thenReturn(List.of(answer));
        when(userClient.getUsersByIds(List.of("reporter-1", "owner-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(user("reporter-1", "Reporter"), user("owner-1", "Owner"))));

        AdminReportResponse response = adminReportService.getReports(null, 1, 10, null).getContent().get(0);

        assertThat(response.reportedContent().content()).isEqualTo("Answer content");
        assertThat(response.reportedContent().questionId()).isEqualTo("question-1");
    }

    @Test
    void getReportsKeepsIdsWhenUserClientFails() {
        Report report = report("report-1", "reporter-1", "answer-1", ReportType.ANSWER);
        CourseAnswer answer = CourseAnswer.builder()
                .userId("owner-1")
                .questionId("question-1")
                .content("Answer content")
                .build();
        answer.setId("answer-1");

        when(reportRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(report)));
        when(answerRepository.findAllById(List.of("answer-1"))).thenReturn(List.of(answer));
        when(userClient.getUsersByIds(List.of("reporter-1", "owner-1"))).thenThrow(new RuntimeException("user unavailable"));

        AdminReportResponse response = adminReportService.getReports(null, 1, 10, null).getContent().get(0);

        assertThat(response.reporterId()).isEqualTo("reporter-1");
        assertThat(response.reporter()).isNull();
        assertThat(response.reportedContent().ownerId()).isEqualTo("owner-1");
        assertThat(response.reportedContent().owner()).isNull();
    }

    @Test
    void getReportsReturnsMissingContentShellWhenContentWasDeleted() {
        Report report = report("report-1", "reporter-1", "review-1", ReportType.REVIEW);

        when(reportRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(report)));
        when(reviewRepository.findAllById(List.of("review-1"))).thenReturn(List.of());
        when(userClient.getUsersByIds(List.of("reporter-1")))
                .thenReturn(new ApiResponse<>(true, "ok", List.of(user("reporter-1", "Reporter"))));

        AdminReportResponse response = adminReportService.getReports(null, 1, 10, null).getContent().get(0);

        assertThat(response.reportedContent().id()).isEqualTo("review-1");
        assertThat(response.reportedContent().type()).isEqualTo(ReportType.REVIEW);
        assertThat(response.reportedContent().content()).isNull();
        assertThat(response.reportedContent().ownerId()).isNull();
    }

    @Test
    void dismissReportDeletesReport() {
        adminReportService.dismissReport("report-1");

        verify(reportRepository).deleteById("report-1");
    }

    @Test
    void deleteReportedContentDelegatesReviewDeletionToReviewService() {
        Report report = report("report-1", "reporter-1", "review-1", ReportType.REVIEW);
        when(reportRepository.findById("report-1")).thenReturn(Optional.of(report));

        adminReportService.deleteReportedContent("report-1");

        verify(reviewService).deleteReviewByAdmin("review-1");
        verify(reviewRepository, never()).deleteById(any());
        verify(reportRepository, never()).deleteByRefIdAndType(any(), any());
    }

    @Test
    void deleteReportedContentDelegatesQuestionDeletionToQnaService() {
        Report report = report("report-1", "reporter-1", "question-1", ReportType.QUESTION);
        when(reportRepository.findById("report-1")).thenReturn(Optional.of(report));

        adminReportService.deleteReportedContent("report-1");

        verify(qnaService).deleteQuestionByAdmin("question-1");
        verify(questionRepository, never()).deleteById(any());
        verify(reportRepository, never()).deleteByRefIdAndType(any(), any());
    }

    @Test
    void deleteReportedContentDelegatesAnswerDeletionToQnaService() {
        Report report = report("report-1", "reporter-1", "answer-1", ReportType.ANSWER);
        when(reportRepository.findById("report-1")).thenReturn(Optional.of(report));

        adminReportService.deleteReportedContent("report-1");

        verify(qnaService).deleteAnswerByAdmin("answer-1");
        verify(answerRepository, never()).deleteById(any());
        verify(reportRepository, never()).deleteByRefIdAndType(any(), any());
    }

    @Test
    void deleteReportedContentThrowsWhenReportDoesNotExist() {
        when(reportRepository.findById("report-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminReportService.deleteReportedContent("report-1"))
                .hasMessage("Report not found");
    }

    private Report report(String id, String reporterId, String refId, ReportType type) {
        Report report = Report.builder()
                .reporterId(reporterId)
                .refId(refId)
                .type(type)
                .reason("Spam")
                .createdAt(LocalDateTime.parse("2025-01-01T10:00:00"))
                .updatedAt(LocalDateTime.parse("2025-01-01T11:00:00"))
                .build();
        report.setId(id);
        return report;
    }

    private UserSummaryResponse user(String userId, String name) {
        return new UserSummaryResponse(userId, name, userId + "@example.com", "STUDENT", "avatar_url");
    }
}
