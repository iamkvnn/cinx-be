package com.cinx.enrollment.service.statistics;

import com.cinx.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatisticsRangeResolverTest {
    private final StatisticsRangeResolver resolver = new StatisticsRangeResolver();

    @Test
    void monthlyRangeAllowsTwelveBuckets() {
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(11);

        StatisticsDateRange range = resolver.resolve(
                StatisticsGroupBy.MONTH,
                startMonth.atDay(1),
                endMonth.atEndOfMonth()
        );

        assertThat(range.groupBy()).isEqualTo(StatisticsGroupBy.MONTH);
        assertThat(range.bucketLabels()).hasSize(12);
    }

    @Test
    void monthlyRangeRejectsThirteenBuckets() {
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(12);

        assertThatThrownBy(() -> resolver.resolve(
                StatisticsGroupBy.MONTH,
                startMonth.atDay(1),
                endMonth.atEndOfMonth()
        )).isInstanceOf(BadRequestException.class);
    }

    @Test
    void dailyRangeAllowsThirtyBuckets() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);

        StatisticsDateRange range = resolver.resolve(StatisticsGroupBy.DAY, startDate, endDate);

        assertThat(range.groupBy()).isEqualTo(StatisticsGroupBy.DAY);
        assertThat(range.bucketLabels()).hasSize(30);
    }

    @Test
    void dailyRangeRejectsThirtyOneBuckets() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        assertThatThrownBy(() -> resolver.resolve(StatisticsGroupBy.DAY, startDate, endDate))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rangeRejectsStartBeforeTwelveMonthLookback() {
        LocalDate endDate = LocalDate.now().minusMonths(12);
        LocalDate startDate = endDate.minusDays(1);

        assertThatThrownBy(() -> resolver.resolve(StatisticsGroupBy.DAY, startDate, endDate))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rangeRejectsSingleDateBoundary() {
        assertThatThrownBy(() -> resolver.resolve(StatisticsGroupBy.DAY, LocalDate.now(), null))
                .isInstanceOf(BadRequestException.class);
    }
}
