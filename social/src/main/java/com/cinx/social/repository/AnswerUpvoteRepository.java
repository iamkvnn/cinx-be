package com.cinx.social.repository;

import com.cinx.social.model.AnswerUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface AnswerUpvoteRepository extends JpaRepository<AnswerUpvote, String> {
    Optional<AnswerUpvote> findByAnswerIdAndUserId(String answerId, String userId);
    boolean existsByAnswerIdAndUserId(String answerId, String userId);
    void deleteByAnswerIdIn(Collection<String> answerIds);
}
