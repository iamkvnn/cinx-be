package com.cinx.enrollment.repository;

import com.cinx.enrollment.model.EnrolledCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface EnrolledCourseRepository extends JpaRepository<EnrolledCourse, String> {
     boolean existsByCourseIdAndUserId(String courseId, String userId);

    Page<EnrolledCourse> findAllByUserId(String userId, Pageable pageable);

    long countByUserId(String userId);

    List<EnrolledCourse> findAllByUserIdAndCourseIdIn(String userId, List<String> courseIds);

    @Query("SELECT e.userId FROM EnrolledCourse e WHERE e.courseId = :courseId")
    List<String> findUserIdsByCourseId(String courseId);

    @Query("""
        SELECT e.courseId, COUNT(e)
        FROM EnrolledCourse e
        WHERE e.createdAt BETWEEN :start AND :end
        GROUP BY e.courseId
        ORDER BY COUNT(e) DESC
    """)
    List<Object[]> findTopEnrolledCourses(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("""
        SELECT COUNT(e)
        FROM EnrolledCourse e
        WHERE e.createdAt BETWEEN :start AND :end
    """)
    long countEnrollmentsBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT COUNT(DISTINCT e.userId)
        FROM EnrolledCourse e
        WHERE e.createdAt BETWEEN :start AND :end
    """)
    long countDistinctLearnersBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT COUNT(e)
        FROM EnrolledCourse e
        WHERE e.courseId IN :courseIds
            AND e.createdAt BETWEEN :start AND :end
    """)
    long countEnrollmentsByCourseIdsBetween(List<String> courseIds, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT COUNT(DISTINCT e.userId)
        FROM EnrolledCourse e
        WHERE e.courseId IN :courseIds
            AND e.createdAt BETWEEN :start AND :end
    """)
    long countDistinctLearnersByCourseIdsBetween(List<String> courseIds, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m-%d'), COUNT(e)
        FROM EnrolledCourse e
        WHERE e.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m-%d')
        ORDER BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m-%d') ASC
    """)
    List<Object[]> aggregateEnrollmentsByDay(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m'), COUNT(e)
        FROM EnrolledCourse e
        WHERE e.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m') ASC
    """)
    List<Object[]> aggregateEnrollmentsByMonth(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m-%d'), COUNT(e)
        FROM EnrolledCourse e
        WHERE e.courseId IN :courseIds
            AND e.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m-%d')
        ORDER BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m-%d') ASC
    """)
    List<Object[]> aggregateEnrollmentsByCourseIdsAndDay(List<String> courseIds, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m'), COUNT(e)
        FROM EnrolledCourse e
        WHERE e.courseId IN :courseIds
            AND e.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', e.createdAt, '%Y-%m') ASC
    """)
    List<Object[]> aggregateEnrollmentsByCourseIdsAndMonth(List<String> courseIds, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT e.courseId, COUNT(e)
        FROM EnrolledCourse e
        WHERE e.courseId IN :courseIds
            AND e.createdAt BETWEEN :start AND :end
        GROUP BY e.courseId
        ORDER BY COUNT(e) DESC
    """)
    List<Object[]> findTopEnrolledCoursesByCourseIds(List<String> courseIds, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT e.userId) FROM EnrolledCourse e WHERE e.courseId IN :courseIds")
    long countDistinctUsersByCourseIds(List<String> courseIds);

    @Query("""
        SELECT e.courseId, COUNT(e)
        FROM EnrolledCourse e
        WHERE e.courseId IN :courseIds
        GROUP BY e.courseId
    """)
    List<Object[]> countEnrollmentsByCourseIds(List<String> courseIds);
}
