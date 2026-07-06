package com.cinx.social.service.admin;

import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.social.client.UserClient;
import com.cinx.social.dto.response.AdminReportResponse;
import com.cinx.social.dto.response.ReportedContentResponse;
import com.cinx.social.dto.response.UserSummaryResponse;
import com.cinx.social.model.*;
import com.cinx.social.repository.*;
import com.cinx.social.service.ICourseQnAService;
import com.cinx.social.service.review.IReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportService implements IAdminReportService {
    private final ReportRepository reportRepository;
    private final ReviewRepository reviewRepository;
    private final CourseQuestionRepository questionRepository;
    private final CourseAnswerRepository answerRepository;
    private final UserClient userClient;
    private final IReviewService reviewService;
    private final ICourseQnAService qnaService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getReports(ReportType type, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page - 1, size, SortConverter.toSort(sort));
        Page<Report> reports;
        if (type != null) {
            reports = reportRepository.findByType(type, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }

        ReportContext context = buildReportContext(reports.getContent());
        return reports.map(report -> toAdminReportResponse(report, context));
    }

    @Override
    @Transactional
    public void dismissReport(String reportId) {
        reportRepository.deleteById(reportId);
    }

    @Override
    @Transactional
    public void deleteReportedContent(String reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found"));

        switch (report.getType()) {
            case REVIEW -> reviewService.deleteReviewByAdmin(report.getRefId());
            case QUESTION -> qnaService.deleteQuestionByAdmin(report.getRefId());
            case ANSWER -> qnaService.deleteAnswerByAdmin(report.getRefId());
        }
    }

    private ReportContext buildReportContext(List<Report> reports) {
        if (reports.isEmpty()) {
            return new ReportContext(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        }

        Map<String, Review> reviewsById = loadReviewsById(reports);
        Map<String, CourseQuestion> questionsById = loadQuestionsById(reports);
        Map<String, CourseAnswer> answersById = loadAnswersById(reports);
        Map<String, UserSummaryResponse> usersById = fetchUsersById(reports, reviewsById.values(), questionsById.values(), answersById.values());

        return new ReportContext(reviewsById, questionsById, answersById, usersById);
    }

    private Map<String, Review> loadReviewsById(List<Report> reports) {
        List<String> ids = refIdsByType(reports, ReportType.REVIEW);
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return reviewRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Review::getId, Function.identity()));
    }

    private Map<String, CourseQuestion> loadQuestionsById(List<Report> reports) {
        List<String> ids = refIdsByType(reports, ReportType.QUESTION);
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return questionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(CourseQuestion::getId, Function.identity()));
    }

    private Map<String, CourseAnswer> loadAnswersById(List<Report> reports) {
        List<String> ids = refIdsByType(reports, ReportType.ANSWER);
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return answerRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(CourseAnswer::getId, Function.identity()));
    }

    private List<String> refIdsByType(List<Report> reports, ReportType type) {
        return reports.stream()
                .filter(report -> report.getType() == type)
                .map(Report::getRefId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private Map<String, UserSummaryResponse> fetchUsersById(
            List<Report> reports,
            Collection<Review> reviews,
            Collection<CourseQuestion> questions,
            Collection<CourseAnswer> answers) {
        List<String> userIds = reports.stream()
                .map(Report::getReporterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        reviews.stream().map(Review::getUserId).filter(Objects::nonNull).forEach(userIds::add);
        questions.stream().map(CourseQuestion::getUserId).filter(Objects::nonNull).forEach(userIds::add);
        answers.stream().map(CourseAnswer::getUserId).filter(Objects::nonNull).forEach(userIds::add);

        List<String> distinctUserIds = userIds.stream().distinct().toList();
        if (distinctUserIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            com.cinx.common.dto.ApiResponse<List<UserSummaryResponse>> response = userClient.getUsersByIds(distinctUserIds);
            if (response == null || !response.success() || response.data() == null) {
                return Collections.emptyMap();
            }
            return response.data().stream()
                    .filter(user -> user.userId() != null)
                    .collect(Collectors.toMap(UserSummaryResponse::userId, Function.identity(), (first, second) -> first));
        } catch (Exception ex) {
            log.warn("Failed to fetch admin report user summaries", ex);
            return Collections.emptyMap();
        }
    }

    private AdminReportResponse toAdminReportResponse(Report report, ReportContext context) {
        return new AdminReportResponse(
                report.getId(),
                report.getReporterId(),
                context.usersById().get(report.getReporterId()),
                report.getType(),
                report.getReason(),
                toReportedContentResponse(report, context),
                report.getCreatedAt(),
                report.getUpdatedAt());
    }

    private ReportedContentResponse toReportedContentResponse(Report report, ReportContext context) {
        return switch (report.getType()) {
            case REVIEW -> toReviewContent(report, context);
            case QUESTION -> toQuestionContent(report, context);
            case ANSWER -> toAnswerContent(report, context);
        };
    }

    private ReportedContentResponse toReviewContent(Report report, ReportContext context) {
        Review review = context.reviewsById().get(report.getRefId());
        if (review == null) {
            return missingContent(report);
        }
        return new ReportedContentResponse(
                report.getRefId(),
                report.getType(),
                review.getUserId(),
                context.usersById().get(review.getUserId()),
                review.getCourseId(),
                null,
                null,
                null,
                review.getContent(),
                review.getRating(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }

    private ReportedContentResponse toQuestionContent(Report report, ReportContext context) {
        CourseQuestion question = context.questionsById().get(report.getRefId());
        if (question == null) {
            return missingContent(report);
        }
        return new ReportedContentResponse(
                report.getRefId(),
                report.getType(),
                question.getUserId(),
                context.usersById().get(question.getUserId()),
                question.getCourseId(),
                question.getLessonId(),
                null,
                question.getTitle(),
                question.getContent(),
                null,
                question.getCreatedAt(),
                question.getUpdatedAt());
    }

    private ReportedContentResponse toAnswerContent(Report report, ReportContext context) {
        CourseAnswer answer = context.answersById().get(report.getRefId());
        if (answer == null) {
            return missingContent(report);
        }
        return new ReportedContentResponse(
                report.getRefId(),
                report.getType(),
                answer.getUserId(),
                context.usersById().get(answer.getUserId()),
                null,
                null,
                answer.getQuestionId(),
                null,
                answer.getContent(),
                null,
                answer.getCreatedAt(),
                answer.getUpdatedAt());
    }

    private ReportedContentResponse missingContent(Report report) {
        return new ReportedContentResponse(
                report.getRefId(),
                report.getType(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private record ReportContext(
            Map<String, Review> reviewsById,
            Map<String, CourseQuestion> questionsById,
            Map<String, CourseAnswer> answersById,
            Map<String, UserSummaryResponse> usersById) {
    }
}
