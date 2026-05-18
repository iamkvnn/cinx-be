package com.cinx.learning.service.dailyGoal;

import com.cinx.common.exception.BadRequestException;
import com.cinx.learning.consts.DailyGoalType;
import com.cinx.learning.dto.request.SetDailyGoalRequest;
import com.cinx.learning.model.LearningItemProgress;
import com.cinx.learning.model.UserDailyGoal;
import com.cinx.learning.repository.LearningItemProgressRepository;
import com.cinx.learning.repository.UserDailyGoalRepository;
import com.cinx.learning.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyGoalServiceTest {

    @Mock
    private UserDailyGoalRepository dailyGoalRepository;

    @Mock
    private LearningItemProgressRepository learningItemProgressRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DailyGoalService dailyGoalService;

    @Test
    void setDailyGoalCreatesGenericGoalByType() {
        LocalDate goalDate = LocalDate.of(2026, 5, 18);
        when(dailyGoalRepository.findByUserIdAndGoalDateAndGoalKey("user-1", goalDate, "XP"))
                .thenReturn(Optional.empty());
        when(dailyGoalRepository.save(any(UserDailyGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDailyGoal goal = dailyGoalService.setDailyGoal(
                "user-1",
                new SetDailyGoalRequest(DailyGoalType.XP, 100, goalDate, null));

        assertThat(goal.getGoalType()).isEqualTo(DailyGoalType.XP);
        assertThat(goal.getGoalKey()).isEqualTo("XP");
        assertThat(goal.getTargetValue()).isEqualTo(100);
        assertThat(goal.getCurrentValue()).isZero();
        assertThat(goal.getIsCompleted()).isFalse();
    }

    @Test
    void setDailyGoalCompletesSpecificLessonGoalWhenLessonAlreadyCompleted() {
        LocalDate goalDate = LocalDate.of(2026, 5, 18);
        LearningItemProgress progress = new LearningItemProgress();
        progress.setIsCompleted(true);

        when(dailyGoalRepository.findByUserIdAndGoalDateAndGoalKey(
                "user-1",
                goalDate,
                "SPECIFIC_LESSON_COMPLETED:lesson-1"))
                .thenReturn(Optional.empty());
        when(learningItemProgressRepository.findByItemIdAndUserId("lesson-1", "user-1"))
                .thenReturn(Optional.of(progress));
        when(dailyGoalRepository.save(any(UserDailyGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDailyGoal goal = dailyGoalService.setDailyGoal(
                "user-1",
                new SetDailyGoalRequest(DailyGoalType.SPECIFIC_LESSON_COMPLETED, null, goalDate, "lesson-1"));

        assertThat(goal.getGoalKey()).isEqualTo("SPECIFIC_LESSON_COMPLETED:lesson-1");
        assertThat(goal.getTargetValue()).isEqualTo(1);
        assertThat(goal.getCurrentValue()).isEqualTo(1);
        assertThat(goal.getIsCompleted()).isTrue();
    }

    @Test
    void setDailyGoalRejectsSpecificLessonGoalWithoutProgress() {
        LocalDate goalDate = LocalDate.of(2026, 5, 18);
        when(dailyGoalRepository.findByUserIdAndGoalDateAndGoalKey(
                "user-1",
                goalDate,
                "SPECIFIC_LESSON_COMPLETED:lesson-1"))
                .thenReturn(Optional.empty());
        when(learningItemProgressRepository.findByItemIdAndUserId("lesson-1", "user-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dailyGoalService.setDailyGoal(
                "user-1",
                new SetDailyGoalRequest(DailyGoalType.SPECIFIC_LESSON_COMPLETED, null, goalDate, "lesson-1")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void recordProgressIncrementsGenericGoalAndAddsXpToUserProfile() {
        UserDailyGoal goal = UserDailyGoal.builder()
                .userId("user-1")
                .goalType(DailyGoalType.XP)
                .goalKey("XP")
                .goalDate(LocalDate.now())
                .targetValue(100)
                .currentValue(50)
                .isCompleted(false)
                .build();
        when(dailyGoalRepository.findByUserIdAndGoalDateAndGoalKey("user-1", LocalDate.now(), "XP"))
                .thenReturn(Optional.of(goal));

        dailyGoalService.recordProgress("user-1", DailyGoalType.XP, 50);

        assertThat(goal.getCurrentValue()).isEqualTo(100);
        assertThat(goal.getIsCompleted()).isTrue();
        verify(dailyGoalRepository).save(goal);
        verify(userService).addXp("user-1", 50);
    }

    @Test
    void recordLessonCompletedOnlyUpdatesMatchingSpecificLessonGoal() {
        UserDailyGoal goal = UserDailyGoal.builder()
                .userId("user-1")
                .goalType(DailyGoalType.SPECIFIC_LESSON_COMPLETED)
                .goalKey("SPECIFIC_LESSON_COMPLETED:lesson-1")
                .goalDate(LocalDate.now())
                .targetItemId("lesson-1")
                .targetValue(1)
                .currentValue(0)
                .isCompleted(false)
                .build();
        when(dailyGoalRepository.findByUserIdAndGoalDateAndGoalTypeAndTargetItemId(
                "user-1",
                LocalDate.now(),
                DailyGoalType.SPECIFIC_LESSON_COMPLETED,
                "lesson-1"))
                .thenReturn(Optional.of(goal));

        dailyGoalService.recordLessonCompleted("user-1", "lesson-1");

        assertThat(goal.getCurrentValue()).isEqualTo(1);
        assertThat(goal.getIsCompleted()).isTrue();
        verify(dailyGoalRepository).save(goal);
    }
}
