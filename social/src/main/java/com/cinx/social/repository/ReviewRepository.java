package com.cinx.social.repository;

import com.cinx.social.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByCourseId(String courseId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") String courseId);
}
