package com.cinx.social.service.review;

import com.cinx.social.dto.request.CreateReportReviewRequest;
import com.cinx.social.dto.request.CreateReviewReactionRequest;
import com.cinx.social.dto.request.CreateReviewRequest;
import com.cinx.social.dto.request.UpdateReviewRequest;
import com.cinx.social.dto.response.ReviewResponse;

import java.util.List;

import org.springframework.data.domain.Page;
import com.cinx.social.dto.request.CreateReviewReplyRequest;
import com.cinx.social.dto.request.UpdateReviewReplyRequest;

public interface IReviewService {
    Page<ReviewResponse> getReviewsByCourseId(String courseId, int page, int size, String sort);
    ReviewResponse getReviewById(String reviewId);
    void createReview(String userId, CreateReviewRequest request);
    void updateReview(String userId, String reviewId, UpdateReviewRequest request);
    void deleteReview(String userId, String reviewId);
    void reportReview(String userId, String reviewId, CreateReportReviewRequest request);
    void reactReview(String userId, String reviewId, CreateReviewReactionRequest request);
    
    void createReviewReply(String userId, String reviewId, CreateReviewReplyRequest request);
    void updateReviewReply(String userId, String replyId, UpdateReviewReplyRequest request);
    void deleteReviewReply(String userId, String replyId);
}
