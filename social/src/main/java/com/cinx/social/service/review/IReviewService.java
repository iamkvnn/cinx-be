package com.cinx.social.service.review;

import com.cinx.social.dto.request.CreateReportReviewRequest;
import com.cinx.social.dto.request.CreateReviewReactionRequest;
import com.cinx.social.dto.request.CreateReviewRequest;
import com.cinx.social.dto.request.UpdateReviewRequest;
import com.cinx.social.dto.response.ReviewResponse;

import java.util.List;

public interface IReviewService {
    List<ReviewResponse> getReviewsByCourseId(String courseId);
    ReviewResponse getReviewById(String reviewId);
    void createReview(CreateReviewRequest request);
    void updateReview(String reviewId, UpdateReviewRequest request);
    void deleteReview(String reviewId);
    void reportReview(String reviewId, CreateReportReviewRequest request);
    void reactReview(String reviewId, CreateReviewReactionRequest request);
}
