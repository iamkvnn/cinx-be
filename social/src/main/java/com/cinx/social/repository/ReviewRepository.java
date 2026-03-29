package com.cinx.social.repository;

import com.cinx.social.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByCourseId(String courseId);
}
