package com.cinx.learning.repository;

import com.cinx.learning.model.VideoNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoNoteRepository extends JpaRepository<VideoNote, String> {
    List<VideoNote> findByUserIdAndLessonId(String userId, String lessonId);
    List<VideoNote> findByUserIdAndCourseId(String userId, String courseId);
}
