package com.cinx.learning.service.reminder;

import com.cinx.learning.messaging.NotificationPublisher;
import com.cinx.learning.model.UserDailyGoal;
import com.cinx.learning.repository.UserDailyGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MorningReminderJob {

    private final UserDailyGoalRepository dailyGoalRepository;
    private final NotificationPublisher notificationPublisher;

    /** Runs every day at 08:00 AM server time. */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendMorningReminders() {
        log.info("Running Morning Reminder Job...");
        LocalDate today = LocalDate.now();

        List<UserDailyGoal> pendingGoals = dailyGoalRepository.findByGoalDateAndIsCompletedFalse(today);
        log.info("Found {} pending daily goals for today", pendingGoals.size());

        for (UserDailyGoal goal : pendingGoals) {
            try {
                notificationPublisher.publishDailyReminderDue(
                        goal.getUserId(),
                        goal.getGoalType(),
                        goal.getTargetValue(),
                        goal.getCurrentValue(),
                        goal.getTargetItemId()
                );
            } catch (Exception e) {
                log.error("Failed to publish reminder event for userId={}: {}", goal.getUserId(), e.getMessage());
            }
        }
    }
}
