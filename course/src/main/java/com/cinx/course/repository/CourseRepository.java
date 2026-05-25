package com.cinx.course.repository;

import com.cinx.course.consts.CourseStatus;
import com.cinx.course.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    """)
    Page<Course> searchAll(
            @Param("query") String query,
            @Param("categoryId") String categoryId,
            @Param("instructorId") String instructorId,
            @Param("rating") Integer rating,
            @Param("priceFrom") Integer priceFrom,
            @Param("priceTo") Integer priceTo,
            @Param("status") CourseStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT c
        FROM Course c
        LEFT JOIN c.category cat
        WHERE c.isPublished = true
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
            AND c.isPublished = true
    """)
    List<Course> findPublishedByIds(@Param("ids") List<String> ids);
}
