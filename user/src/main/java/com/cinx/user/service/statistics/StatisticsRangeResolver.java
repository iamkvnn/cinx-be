package com.cinx.user.service.statistics;

import com.cinx.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class StatisticsRangeResolver {
    private static final int MAX_MONTH_BUCKETS = 12;
    private static final int MAX_DAY_BUCKETS = 30;
    private static final int MAX_LOOKBACK_MONTHS = 12;

    public StatisticsDateRange resolve(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        StatisticsGroupBy normalizedGroupBy = groupBy != null ? groupBy : StatisticsGroupBy.MONTH;
        if (startDate == null && endDate == null) {
            return normalizedGroupBy == StatisticsGroupBy.DAY
                    ? dailyRange(LocalDate.now().minusDays(MAX_DAY_BUCKETS - 1L), LocalDate.now())
                    : monthlyRange(YearMonth.now().minusMonths(MAX_MONTH_BUCKETS - 1L), YearMonth.now());
        }
        if (startDate == null || endDate == null) {
            throw new BadRequestException("startDate and endDate must be provided together");
        }
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("endDate must not be before startDate");
        }
        validateLookback(startDate);
        return normalizedGroupBy == StatisticsGroupBy.DAY
                ? dailyRange(startDate, endDate)
                : monthlyRange(YearMonth.from(startDate), YearMonth.from(endDate));
    }

    private StatisticsDateRange monthlyRange(YearMonth startMonth, YearMonth endMonth) {
        long bucketCount = ChronoUnit.MONTHS.between(startMonth, endMonth) + 1;
        if (bucketCount > MAX_MONTH_BUCKETS) {
            throw new BadRequestException("Monthly statistics cannot exceed 12 months");
        }
        List<String> labels = new ArrayList<>();
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            labels.add(current.toString());
            current = current.plusMonths(1);
        }
        return new StatisticsDateRange(
                startMonth.atDay(1).atStartOfDay(),
                endMonth.atEndOfMonth().atTime(23, 59, 59, 999999999),
                StatisticsGroupBy.MONTH,
                labels
        );
    }

    private StatisticsDateRange dailyRange(LocalDate startDate, LocalDate endDate) {
        long bucketCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (bucketCount > MAX_DAY_BUCKETS) {
            throw new BadRequestException("Daily statistics cannot exceed 30 days");
        }
        List<String> labels = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            labels.add(current.toString());
            current = current.plusDays(1);
        }
        return new StatisticsDateRange(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59, 999999999),
                StatisticsGroupBy.DAY,
                labels
        );
    }

    private void validateLookback(LocalDate startDate) {
        LocalDate earliest = LocalDate.now().minusMonths(MAX_LOOKBACK_MONTHS);
        if (startDate.isBefore(earliest)) {
            throw new BadRequestException("Statistics cannot look back more than 12 months");
        }
    }
}
