package com.cinx.course.repository;

import com.cinx.course.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, String> {
    @Query("SELECT q FROM QuizQuestion q LEFT JOIN FETCH q.options WHERE q.quizLesson.lessonId = :quizLessonId")
    List<QuizQuestion> findAllByQuizLessonId(String quizLessonId);

    @Query("SELECT q FROM QuizQuestion q LEFT JOIN FETCH q.options WHERE q.quizLesson.lessonId = :quizLessonId AND q.needSync = true")
    List<QuizQuestion> findAllByQuizLessonIdAndNeedSync(String quizLessonId);

    @Query("SELECT q FROM QuizQuestion q LEFT JOIN FETCH q.options WHERE q.id = :id AND q.quizLesson.lessonId = :quizLessonId")
    Optional<QuizQuestion> findByIdAndQuizLessonId(String id, String quizLessonId);

    @Query("SELECT COUNT(q) FROM QuizQuestion q WHERE q.quizLesson.lessonId = :quizLessonId")
    int countByQuizLessonId(String quizLessonId);
}
