package com.cinx.user.service.statistics;

import com.cinx.user.dto.response.StatisticsByTimeResponse;
import com.cinx.user.dto.response.UserStatisticsOverviewResponse;
import com.cinx.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserStatisticsService implements IUserStatisticsService {
    private final UserRepository userRepository;
    private final StatisticsRangeResolver statisticsRangeResolver;

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsOverviewResponse getOverview(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        StatisticsDateRange range = statisticsRangeResolver.resolve(groupBy, startDate, endDate);
        List<Object[]> userRows = range.groupByDay()
                ? userRepository.aggregateNewUsersByDay(range.start(), range.end())
                : userRepository.aggregateNewUsersByMonth(range.start(), range.end());

        return new UserStatisticsOverviewResponse(
                userRepository.countUsersBetween(range.start(), range.end()),
                toStringLongMap(userRepository.countUsersByRoleBetween(range.start(), range.end())),
                toInstructorVerificationStatusMap(userRepository.countInstructorsByVerificationStatusBetween(range.start(), range.end())),
                fillByTime(range, userRows),
                userRepository.countTotalUsers()
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

    private Map<String, Long> toInstructorVerificationStatusMap(List<Object[]> rows) {
        Map<String, Long> values = new LinkedHashMap<>();
        values.put("verified", 0L);
        values.put("unverified", 0L);
        rows.forEach(row -> {
            Boolean verified = (Boolean) row[0];
            String status = Boolean.TRUE.equals(verified) ? "verified" : "unverified";
            values.put(status, ((Number) row[1]).longValue());
        });
        return values;
    }
}
