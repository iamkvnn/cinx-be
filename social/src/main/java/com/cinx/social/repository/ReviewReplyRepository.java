package com.cinx.social.repository;

import com.cinx.social.model.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, String> {
    Optional<ReviewReply> findByReviewId(String reviewId);
}
