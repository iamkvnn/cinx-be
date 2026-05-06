package com.cinx.social.repository;

import com.cinx.social.model.CourseAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface CourseAnswerRepository extends JpaRepository<CourseAnswer, String> {
    Page<CourseAnswer> findByQuestionIdAndParentAnswerIdIsNullOrderByCreatedAtAsc(String questionId, Pageable pageable);
    Page<CourseAnswer> findByParentAnswerIdOrderByCreatedAtAsc(String parentAnswerId, Pageable pageable);
    int countByQuestionId(String questionId);
    int countByParentAnswerId(String parentAnswerId);
}
