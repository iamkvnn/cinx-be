package com.cinx.enrollment.service.statistics;

import com.cinx.common.exception.BadRequestException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class StatisticsRangeResolver {
    static final int DEFAULT_MONTH_BUCKETS = 12;
    static final int MAX_MONTH_BUCKETS = 12;
    static final int DEFAULT_DAY_BUCKETS = 30;
    static final int MAX_DAY_BUCKETS = 30;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public StatisticsDateRange resolve(StatisticsGroupBy groupBy, LocalDate startDate, LocalDate endDate) {
        StatisticsGroupBy normalizedGroupBy = groupBy != null ? groupBy : StatisticsGroupBy.MONTH;
        if (startDate == null && endDate == null) {
            return normalizedGroupBy == StatisticsGroupBy.DAY
                    ? resolveLastDays(DEFAULT_DAY_BUCKETS)
                    : resolveLastMonths(DEFAULT_MONTH_BUCKETS);
        }
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required together");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        return normalizedGroupBy == StatisticsGroupBy.DAY
                ? resolveDaily(startDate, endDate)
                : resolveMonthly(startDate, endDate);
    }

    private StatisticsDateRange resolveLastMonths(int months) {
        if (months <= 0) {
            throw new BadRequestException("Months must be greater than 0");
        }
        if (months > MAX_MONTH_BUCKETS) {
            throw new BadRequestException("Monthly statistics cannot exceed 12 months");
        }
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(months - 1L);
        return monthlyRange(startMonth, endMonth);
    }

    public YearMonth resolveDashboardMonth(Integer year, Integer month) {
        YearMonth targetMonth = year != null && month != null ? toYearMonth(year, month) : YearMonth.now();
        validateMonthlyLookback(targetMonth);
        return targetMonth;
    }

    private StatisticsDateRange resolveLastDays(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        return dailyRange(startDate, endDate);
    }

    private StatisticsDateRange resolveMonthly(LocalDate startDate, LocalDate endDate) {
        return monthlyRange(YearMonth.from(startDate), YearMonth.from(endDate));
    }

    private StatisticsDateRange resolveDaily(LocalDate startDate, LocalDate endDate) {
        long dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1L;
        if (dayCount > MAX_DAY_BUCKETS) {
            throw new BadRequestException("Daily statistics cannot exceed 30 days");
        }
        validateDailyLookback(startDate);
        return dailyRange(startDate, endDate);
    }

    private StatisticsDateRange monthlyRange(YearMonth startMonth, YearMonth endMonth) {
        if (startMonth.isAfter(endMonth)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        long monthCount = ChronoUnit.MONTHS.between(startMonth, endMonth) + 1L;
        if (monthCount > MAX_MONTH_BUCKETS) {
            throw new BadRequestException("Monthly statistics cannot exceed 12 months");
        }
        validateMonthlyLookback(startMonth);

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < monthCount; i++) {
            labels.add(startMonth.plusMonths(i).format(MONTH_FORMATTER));
        }
        return new StatisticsDateRange(
                startMonth.atDay(1).atStartOfDay(),
                endMonth.atEndOfMonth().atTime(23, 59, 59, 999999999),
                StatisticsGroupBy.MONTH,
                labels
        );
    }

    private StatisticsDateRange dailyRange(LocalDate startDate, LocalDate endDate) {
        long dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1L;
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            labels.add(startDate.plusDays(i).format(DAY_FORMATTER));
        }
        return new StatisticsDateRange(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59, 999999999),
                StatisticsGroupBy.DAY,
                labels
        );
    }

    private void validateMonthlyLookback(YearMonth startMonth) {
        YearMonth earliestMonth = YearMonth.now().minusMonths(MAX_MONTH_BUCKETS - 1L);
        if (startMonth.isBefore(earliestMonth)) {
            throw new BadRequestException("Statistics cannot look back more than 12 months");
        }
    }

    private void validateDailyLookback(LocalDate startDate) {
        LocalDate earliestDate = LocalDate.now().minusMonths(MAX_MONTH_BUCKETS);
        if (startDate.isBefore(earliestDate)) {
            throw new BadRequestException("Statistics cannot look back more than 12 months");
        }
    }

    private YearMonth toYearMonth(Integer year, Integer month) {
        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException ex) {
            throw new BadRequestException("Invalid year or month");
        }
    }
}
