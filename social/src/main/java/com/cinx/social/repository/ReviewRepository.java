package com.cinx.social.repository;

import com.cinx.social.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository extends JpaRepository<Review, String> {
    Page<Review> findByCourseId(String courseId, Pageable pageable);

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    Optional<Review> findByUserIdAndCourseId(String userId, String courseId);

    Long countByCourseId(String courseId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") String courseId);

    @Query("""
        SELECT r.rating, COUNT(r)
        FROM Review r
        WHERE r.courseId = :courseId
        GROUP BY r.rating
        ORDER BY r.rating ASC
    """)
    List<Object[]> countRatingsByCourseId(String courseId);
}
