package com.cinx.social.repository;

import com.cinx.social.model.ReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, String> {
    Optional<ReviewReaction> findByUserIdAndReviewId(String userId, String reviewId);
    List<ReviewReaction> findByReviewId(String reviewId);
    List<ReviewReaction> findByReviewIdIn(List<String> reviewIds);
}
