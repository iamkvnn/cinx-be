package com.cinx.course.repository;

import com.cinx.course.model.VideoQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoQuestionRepository extends JpaRepository<VideoQuestion, String> {
    List<VideoQuestion> findByVideoLessonLessonIdOrderByTimestampSecondsAsc(String lessonId);
    Optional<VideoQuestion> findByIdAndVideoLessonLessonId(String id, String lessonId);
}
