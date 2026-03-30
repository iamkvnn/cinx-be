package com.cinx.learning.service.streak;

import com.cinx.learning.dto.response.UserStreakResponse;
import com.cinx.learning.mapper.UserStreakMapper;
import com.cinx.learning.model.UserStreak;
import com.cinx.learning.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StreakService implements IStreakService {

    private final UserStreakRepository streakRepository;
    private final UserStreakMapper streakMapper;

    @Override
    @Transactional(readOnly = true)
    public UserStreakResponse getUserStreak(String userId) {
        UserStreak streak = streakRepository.findByUserId(userId)
                .orElseGet(() -> UserStreak.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .highestStreak(0)
                        .build());
        return streakMapper.toDto(streak);
    }

    @Override
    public void updateStreakOnActivity(String userId) {
        LocalDate today = LocalDate.now();
        Optional<UserStreak> streakOpt = streakRepository.findByUserId(userId);

        UserStreak streak;
        if (streakOpt.isPresent()) {
            streak = streakOpt.get();
            LocalDate lastActivity = streak.getLastActivityDate();

            if (lastActivity == null || lastActivity.isBefore(today.minusDays(1))) {
                // Streak broken or just started
                streak.setCurrentStreak(1);
            } else if (lastActivity.equals(today.minusDays(1))) {
                // Continued streak
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            }
            // IF lastActivity.equals(today) do nothing to the count
            
            if (streak.getCurrentStreak() > streak.getHighestStreak()) {
                streak.setHighestStreak(streak.getCurrentStreak());
            }
            streak.setLastActivityDate(today);
        } else {
            streak = UserStreak.builder()
                    .userId(userId)
                    .currentStreak(1)
                    .highestStreak(1)
                    .lastActivityDate(today)
                    .build();
        }
        streakRepository.save(streak);
    }
}
