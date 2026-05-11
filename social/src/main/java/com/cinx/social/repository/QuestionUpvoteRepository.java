package com.cinx.social.repository;

import com.cinx.social.model.QuestionUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionUpvoteRepository extends JpaRepository<QuestionUpvote, String> {
    Optional<QuestionUpvote> findByQuestionIdAndUserId(String questionId, String userId);
    boolean existsByQuestionIdAndUserId(String questionId, String userId);
}
