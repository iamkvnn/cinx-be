package com.cinx.social.repository;

import com.cinx.social.model.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, String> {
    Optional<ReviewReply> findByReviewId(String reviewId);

    @Query("""
        SELECT COUNT(rr)
        FROM ReviewReply rr, Review r
        WHERE rr.reviewId = r.id
            AND r.courseId = :courseId
    """)
    Long countRepliesByCourseId(String courseId);
}
