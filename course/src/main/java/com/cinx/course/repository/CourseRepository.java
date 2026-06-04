package com.cinx.course.repository;

import com.cinx.course.consts.CourseStatus;
import com.cinx.course.consts.CoursePublishStatus;
import com.cinx.course.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
    @Query("""
        SELECT c
        FROM Course c
        LEFT JOIN c.category cat
        WHERE
            (:query IS NULL OR
                c.title LIKE %:query%
                OR c.description LIKE %:query%)
            AND (:categoryId IS NULL OR cat.id = :categoryId)
            AND (:instructorId IS NULL OR c.instructorId = :instructorId)
            AND (:rating IS NULL OR c.rating >= :rating)
            AND (:priceFrom IS NULL OR c.price >= :priceFrom)
            AND (:priceTo IS NULL OR c.price <= :priceTo)
            AND (:status IS NULL OR c.status = :status)
            AND (:publishStatus IS NULL OR c.publishStatus = :publishStatus)
    """)
    Page<Course> searchAll(
            @Param("query") String query,
            @Param("categoryId") String categoryId,
            @Param("instructorId") String instructorId,
            @Param("rating") Integer rating,
            @Param("priceFrom") Integer priceFrom,
            @Param("priceTo") Integer priceTo,
            @Param("status") CourseStatus status,
            @Param("publishStatus") CoursePublishStatus publishStatus,
            Pageable pageable
    );

    @Query("""
        SELECT c
        FROM Course c
        LEFT JOIN c.category cat
        WHERE c.status = com.cinx.course.consts.CourseStatus.PUBLISHED
            AND (:query IS NULL OR
                c.title LIKE %:query%
                OR c.description LIKE %:query%)
            AND (:categoryId IS NULL OR cat.id = :categoryId)
            AND (:instructorId IS NULL OR c.instructorId = :instructorId)
            AND (:rating IS NULL OR c.rating >= :rating)
            AND (:priceFrom IS NULL OR c.price >= :priceFrom)
            AND (:priceTo IS NULL OR c.price <= :priceTo)
    """)
    Page<Course> searchPublished(
            @Param("query") String query,
            @Param("categoryId") String categoryId,
            @Param("instructorId") String instructorId,
            @Param("rating") Integer rating,
            @Param("priceFrom") Integer priceFrom,
            @Param("priceTo") Integer priceTo,
            Pageable pageable
    );

    @Query("""
        SELECT c
        FROM Course c
        WHERE c.id IN :ids
            AND c.status = com.cinx.course.consts.CourseStatus.PUBLISHED
    """)
    List<Course> findPublishedByIds(@Param("ids") List<String> ids);

    @Query("""
        SELECT c
        FROM Course c
        WHERE c.id IN :ids
            AND c.status IN (com.cinx.course.consts.CourseStatus.PUBLISHED, com.cinx.course.consts.CourseStatus.ARCHIVED)
    """)
    List<Course> findEnrolledReadableByIds(@Param("ids") List<String> ids);

    long countByInstructorId(String instructorId);

    long countByInstructorIdAndStatus(String instructorId, CourseStatus status);

    long countByStatus(CourseStatus status);

    @Query("SELECT AVG(c.rating) FROM Course c WHERE c.instructorId = :instructorId AND c.rating IS NOT NULL")
    Double averageRatingByInstructorId(@Param("instructorId") String instructorId);

    @Query("SELECT COALESCE(SUM(c.enrollmentCount), 0) FROM Course c WHERE c.instructorId = :instructorId")
    Long sumEnrollmentCountByInstructorId(@Param("instructorId") String instructorId);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdAt BETWEEN :start AND :end")
    long countCreatedCoursesBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructorId = :instructorId AND c.createdAt BETWEEN :start AND :end")
    long countCreatedCoursesByInstructorBetween(String instructorId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT c.status, COUNT(c)
        FROM Course c
        WHERE c.createdAt BETWEEN :start AND :end
        GROUP BY c.status
    """)
    List<Object[]> countCreatedCoursesByStatusBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT c.status, COUNT(c)
        FROM Course c
        GROUP BY c.status
    """)
    List<Object[]> countCurrentCoursesByStatus();

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m-%d'), COUNT(c)
        FROM Course c
        WHERE c.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m-%d')
        ORDER BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m-%d') ASC
    """)
    List<Object[]> aggregateCreatedCoursesByDay(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m'), COUNT(c)
        FROM Course c
        WHERE c.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m') ASC
    """)
    List<Object[]> aggregateCreatedCoursesByMonth(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m-%d'), COUNT(c)
        FROM Course c
        WHERE c.instructorId = :instructorId
            AND c.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m-%d')
        ORDER BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m-%d') ASC
    """)
    List<Object[]> aggregateCreatedCoursesByInstructorAndDay(String instructorId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m'), COUNT(c)
        FROM Course c
        WHERE c.instructorId = :instructorId
            AND c.createdAt BETWEEN :start AND :end
        GROUP BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m') ASC
    """)
    List<Object[]> aggregateCreatedCoursesByInstructorAndMonth(String instructorId, LocalDateTime start, LocalDateTime end);
}
