package com.cinx.learning.service.reminder;

import com.cinx.learning.model.UserDailyGoal;
import com.cinx.learning.repository.UserDailyGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MorningReminderJob {

    private final UserDailyGoalRepository dailyGoalRepository;
    private final RabbitTemplate rabbitTemplate;

    // Run every day at 08:00 AM server time
    @Scheduled(cron = "0 0 8 * * *")
    public void sendMorningReminders() {
        log.info("Running Morning Reminder Job...");
        LocalDate today = LocalDate.now();

        // Find users whose goal for today is not completed
        // Oh wait, if it's 8 AM, they probably haven't even started today's goal yet.
        // It's a general reminder for users who have active goals.
        // For simplicity we can fetch all users who set a goal for today, or just users who haven't completed.
        List<UserDailyGoal> pendingGoals = dailyGoalRepository.findByGoalDateAndIsCompletedFalse(today);

        log.info("Found {} pending daily goals for today", pendingGoals.size());

        for (UserDailyGoal goal : pendingGoals) {
            sendReminderEvent(goal.getUserId(), goal.getTargetXp(), goal.getCurrentXp());
        }
    }

    private void sendReminderEvent(String userId, Integer targetXp, Integer currentXp) {
        try {
            Map<String, Object> payload = Map.of(
                    "userId", userId,
                    "targetXp", targetXp,
                    "currentXp", currentXp,
                    "type", "DAILY_GOAL_REMINDER",
                    "title", "Don't break your momentum!",
                    "message", "You have a goal of " + targetXp + " XP today. Keep learning!"
            );

            rabbitTemplate.convertAndSend("learning.events.exchange", "learning.reminder.morning", payload);
            log.info("Sent morning reminder event for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send reminder event for user {}: {}", userId, e.getMessage());
        }
    }
}