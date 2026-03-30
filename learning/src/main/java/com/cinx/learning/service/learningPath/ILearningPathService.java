package com.cinx.learning.service.learningPath;

import com.cinx.learning.dto.request.LearningPathRequest;
import com.cinx.learning.dto.response.LearningPathResponse;
import com.cinx.learning.model.UserLearningPath;

public interface ILearningPathService {
    LearningPathResponse createLearningPath(String userId, LearningPathRequest request);
    LearningPathResponse getActiveLearningPath(String userId);
    void dropActiveLearningPath(String userId);
    void activatePendingPathForCourse(String userId, String courseId);
    void updatePathProgress(String userId, String lessonId);
}
