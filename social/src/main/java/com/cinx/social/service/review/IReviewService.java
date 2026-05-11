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
    void createReview(CreateReviewRequest request);
    void updateReview(String reviewId, UpdateReviewRequest request);
    void deleteReview(String reviewId);
    void reportReview(String reviewId, CreateReportReviewRequest request);
    void reactReview(String reviewId, CreateReviewReactionRequest request);
    
    void createReviewReply(String reviewId, CreateReviewReplyRequest request);
    void updateReviewReply(String replyId, UpdateReviewReplyRequest request);
    void deleteReviewReply(String replyId);
}
