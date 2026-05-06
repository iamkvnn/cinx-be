package com.cinx.learning.service.learningPath;

import com.cinx.learning.dto.request.LearningPathRequest;
import com.cinx.learning.dto.response.LearningPathResponse;
import com.cinx.learning.model.UserLearningPath;
import java.util.List;

public interface ILearningPathService {
    LearningPathResponse createLearningPath(String userId, LearningPathRequest request);
    LearningPathResponse getActiveLearningPath(String userId);
    List<LearningPathResponse> getLearningPaths(String userId);
    LearningPathResponse getLearningPath(String userId, String id);
    void dropActiveLearningPath(String userId);
    void activatePendingPathForCourse(String userId, String courseId);
    void updatePathProgress(String userId, String lessonId);

    /**
     * Re-evaluate learning-path item unlock state for a single user after
     * prerequisite or lesson-order changes in a course.  Preserves completed
     * items; only marks not-yet-started items as unlocked or re-locked.
     */
    void refreshPrerequisiteUnlocks(String userId, String courseId);
}
