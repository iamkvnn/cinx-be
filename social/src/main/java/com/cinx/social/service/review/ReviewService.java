package com.cinx.social.service.review;

import com.cinx.common.exception.NotFoundException;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.social.dto.request.CreateReportReviewRequest;
import com.cinx.social.dto.request.CreateReviewReactionRequest;
import com.cinx.social.dto.request.CreateReviewRequest;
import com.cinx.social.dto.request.UpdateReviewRequest;
import com.cinx.social.dto.response.ReviewResponse;
import com.cinx.social.mapper.ReviewMapper;
import com.cinx.social.model.Review;
import com.cinx.social.model.ReviewReaction;
import com.cinx.social.model.ReviewReport;
import com.cinx.social.repository.ReviewReactionRepository;
import com.cinx.social.repository.ReviewReportRepository;
import com.cinx.social.repository.ReviewRepository;
import com.cinx.social.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final ReviewReactionRepository reviewReactionRepository;
    private final ReviewMapper reviewMapper;
    private final CourseService courseService;

    @Override
    public List<ReviewResponse> getReviewsByCourseId(String courseId) {
        return reviewRepository.findByCourseId(courseId).stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Override
    public ReviewResponse getReviewById(String reviewId) {
        return reviewRepository.findById(reviewId)
                .map(reviewMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Review not found"));
    }

    @Override
    public void createReview(CreateReviewRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        reviewRepository.save(Review.builder()
                .courseId(request.courseId())
                .userId(userId)
                .content(request.content())
                .rating(request.rating())
                .build());

        updateCourseRatingInCourseService(request.courseId());
    }

    private void updateCourseRatingInCourseService(String courseId) {
        Double averageRating = reviewRepository.getAverageRatingByCourseId(courseId);
        if (averageRating == null) return;
        try {
            courseService.updateCourseRating(courseId, averageRating);
        } catch (Exception e) {
            // Log issue
        }
    }

    @Override
    public void updateReview(String reviewId, UpdateReviewRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        if (!review.getUserId().equals(userId)) {
            throw new NotFoundException("Review not found");
        }
        review.setContent(request.content());
        review.setRating(request.rating());
        reviewRepository.save(review);
        updateCourseRatingInCourseService(review.getCourseId());
    }

    @Override
    public void deleteReview(String reviewId) {
        String userId = AuthenticationUtil.extractUserId();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        if (!review.getUserId().equals(userId)) {
            throw new NotFoundException("Review not found");
        }
        reviewRepository.deleteById(reviewId);
        updateCourseRatingInCourseService(review.getCourseId());
    }

    @Override
    public void reportReview(String reviewId, CreateReportReviewRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        reviewReportRepository.save(ReviewReport.builder()
                .reviewId(reviewId)
                .reporterId(userId)
                .reason(request.reason())
                .build());
    }

    @Override
    public void reactReview(String reviewId, CreateReviewReactionRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        ReviewReaction existingReaction = reviewReactionRepository.findByUserIdAndReviewId(userId, reviewId)
                .orElse(null);
        if (existingReaction != null) {
            existingReaction.setLiked(request.liked());
            reviewReactionRepository.save(existingReaction);
            return;
        }
        reviewReactionRepository.save(ReviewReaction.builder()
                .reviewId(reviewId)
                .userId(userId)
                .liked(request.liked())
                .build());
    }
}
