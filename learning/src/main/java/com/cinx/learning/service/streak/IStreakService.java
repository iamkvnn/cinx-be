package com.cinx.learning.service.streak;

import com.cinx.learning.dto.response.UserStreakResponse;

public interface IStreakService {
    UserStreakResponse getUserStreak(String userId);
    void updateStreakOnActivity(String userId);
}
