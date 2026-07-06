package com.cinx.social.repository;

import com.cinx.social.model.CourseQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseQuestionRepository extends JpaRepository<CourseQuestion, String> {
    Page<CourseQuestion> findByCourseId(String courseId, Pageable pageable);
    Page<CourseQuestion> findByCourseIdAndLessonId(String courseId, String lessonId, Pageable pageable);

    @Query("""
        SELECT q
        FROM CourseQuestion q
        WHERE q.courseId = :courseId
          AND (:lessonId IS NULL OR q.lessonId = :lessonId)
          AND (:query IS NULL OR q.title LIKE %:query% OR q.content LIKE %:query% OR q.userId LIKE %:query%)
    """)
    Page<CourseQuestion> search(String courseId, String lessonId, String query, Pageable pageable);

    @Query("""
        SELECT COUNT(q)
        FROM CourseQuestion q
        WHERE q.courseId = :courseId
            AND q.createdAt BETWEEN :start AND :end
    """)
    Long countQuestionsByCourseIdBetween(String courseId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT COUNT(DISTINCT q.id)
        FROM CourseQuestion q
        JOIN CourseAnswer a ON a.questionId = q.id
        WHERE q.courseId = :courseId
            AND q.createdAt BETWEEN :start AND :end
            AND a.isInstructorAnswer = true
    """)
    Long countQuestionsWithInstructorAnswerBetween(String courseId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', q.createdAt, '%Y-%m-%d'), COUNT(q)
        FROM CourseQuestion q
        WHERE q.courseId = :courseId
            AND q.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', q.createdAt, '%Y-%m-%d')
        ORDER BY FUNCTION('DATE_FORMAT', q.createdAt, '%Y-%m-%d') ASC
    """)
    List<Object[]> aggregateQuestionsByDay(String courseId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', q.createdAt, '%Y-%m'), COUNT(q)
        FROM CourseQuestion q
        WHERE q.courseId = :courseId
            AND q.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', q.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', q.createdAt, '%Y-%m') ASC
    """)
    List<Object[]> aggregateQuestionsByMonth(String courseId, LocalDateTime start, LocalDateTime end);
}
