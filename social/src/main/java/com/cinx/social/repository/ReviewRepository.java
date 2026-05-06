package com.cinx.social.repository;

import com.cinx.social.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository extends JpaRepository<Review, String> {
    Page<Review> findByCourseId(String courseId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") String courseId);
}
