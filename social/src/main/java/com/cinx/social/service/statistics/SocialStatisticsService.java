package com.cinx.social.service.statistics;

import com.cinx.social.dto.response.CourseQnAStatisticsResponse;
import com.cinx.social.dto.response.ReportStatisticsOverviewResponse;
import com.cinx.social.dto.response.ReviewStatisticsResponse;
import com.cinx.social.dto.response.StatisticsByTimeResponse;
import com.cinx.social.dto.response.TopReportedRefResponse;
import com.cinx.social.repository.CourseAnswerRepository;
import com.cinx.social.repository.CourseQuestionRepository;
import com.cinx.social.repository.ReportRepository;
import com.cinx.social.repository.ReviewReplyRepository;
import com.cinx.social.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialStatisticsService implements ISocialStatisticsService {
    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final CourseQuestionRepository courseQuestionRepository;
    private final CourseAnswerRepository courseAnswerRepository;
    private final ReportRepository reportRepository;
    private final StatisticsRangeResolver statisticsRangeResolver = new StatisticsRangeResolver();

    @Override
    @Transactional(readOnly = true)
    public ReviewStatisticsResponse getReviewStatistics(String courseId) {
        Long reviewCount = reviewRepository.countByCourseId(courseId);
        Long replyCount = reviewReplyRepository.countRepliesByCourseId(courseId);
        Double averageRating = reviewRepository.getAverageRatingByCourseId(courseId);
        return new ReviewStatisticsResponse(
                reviewCount,
                averageRating != null ? averageRating : 0.0,
                toStringLongMap(reviewRepository.countRatingsByCourseId(courseId)),
                reviewCount > 0 ? replyCount * 100.0 / reviewCount : 0.0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CourseQnAStatisticsResponse getCourseQnAStatistics(String courseId, StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        StatisticsDateRange range = statisticsRangeResolver.resolve(groupBy, startDate, endDate);
        Long questionsInRange = courseQuestionRepository.countQuestionsByCourseIdBetween(courseId, range.start(), range.end());
        Long instructorAnsweredQuestions = courseQuestionRepository.countQuestionsWithInstructorAnswerBetween(courseId, range.start(), range.end());
        List<Object[]> questionRows = range.groupByDay()
                ? courseQuestionRepository.aggregateQuestionsByDay(courseId, range.start(), range.end())
                : courseQuestionRepository.aggregateQuestionsByMonth(courseId, range.start(), range.end());
        return new CourseQnAStatisticsResponse(
                questionsInRange,
                courseAnswerRepository.countAnswersByCourseIdBetween(courseId, range.start(), range.end()),
                questionsInRange - instructorAnsweredQuestions,
                questionsInRange > 0 ? instructorAnsweredQuestions * 100.0 / questionsInRange : 0.0,
                fillByTime(range, questionRows)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReportStatisticsOverviewResponse getReportOverview(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        StatisticsDateRange range = statisticsRangeResolver.resolve(groupBy, startDate, endDate);
        List<Object[]> reportRows = range.groupByDay()
                ? reportRepository.aggregateReportsByDay(range.start(), range.end())
                : reportRepository.aggregateReportsByMonth(range.start(), range.end());
        return new ReportStatisticsOverviewResponse(
                reportRepository.countReportsBetween(range.start(), range.end()),
                toStringLongMap(reportRepository.countReportsByTypeBetween(range.start(), range.end())),
                fillByTime(range, reportRows),
                toTopReportedRefs(reportRepository.findTopReportedRefs(range.start(), range.end(), PageRequest.of(0, 5)))
        );
    }

    private List<StatisticsByTimeResponse> fillByTime(StatisticsDateRange range, List<Object[]> rows) {
        Map<String, Long> valuesByLabel = new LinkedHashMap<>();
        range.bucketLabels().forEach(label -> valuesByLabel.put(label, 0L));
        rows.forEach(row -> valuesByLabel.put((String) row[0], ((Number) row[1]).longValue()));
        return valuesByLabel.entrySet().stream()
                .map(entry -> new StatisticsByTimeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Map<String, Long> toStringLongMap(List<Object[]> rows) {
        Map<String, Long> values = new LinkedHashMap<>();
        rows.forEach(row -> values.put(String.valueOf(row[0]), ((Number) row[1]).longValue()));
        return values;
    }

    private List<TopReportedRefResponse> toTopReportedRefs(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new TopReportedRefResponse(
                        (String) row[0],
                        (com.cinx.social.model.ReportType) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }
}
