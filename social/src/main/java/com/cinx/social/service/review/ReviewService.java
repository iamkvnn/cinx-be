package com.cinx.social.service.review;

import com.cinx.common.exception.NotFoundException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.mapper.SortConverter;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.social.dto.request.CreateReportReviewRequest;
import com.cinx.social.dto.request.CreateReviewReactionRequest;
import com.cinx.social.dto.request.CreateReviewRequest;
import com.cinx.social.dto.request.UpdateReviewRequest;
import com.cinx.social.dto.response.ReviewResponse;
import com.cinx.social.mapper.ReviewMapper;
import com.cinx.social.model.Report;
import com.cinx.social.model.ReportType;
import com.cinx.social.model.Review;
import com.cinx.social.model.ReviewReaction;
import com.cinx.social.repository.ReportRepository;
import com.cinx.social.repository.ReviewReactionRepository;
import com.cinx.social.repository.ReviewRepository;
import com.cinx.social.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cinx.social.dto.request.CreateReviewReplyRequest;
import com.cinx.social.dto.request.UpdateReviewReplyRequest;
import com.cinx.social.dto.response.ReviewReplyDto;
import com.cinx.social.model.ReviewReply;
import com.cinx.social.repository.ReviewReplyRepository;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.social.dto.response.CourseResponse;
import com.cinx.common.dto.ApiResponse;

@RequiredArgsConstructor
@Service
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final ReviewReactionRepository reviewReactionRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ReviewMapper reviewMapper;
    private final CourseService courseService;

    @Override
    public Page<ReviewResponse> getReviewsByCourseId(String courseId, int page, int size, String sort) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, size, SortConverter.toSort(sort));
        return reviewRepository.findByCourseId(courseId, pageable).map(review -> {
            ReviewResponse dto = reviewMapper.toDto(review);
            ReviewReply reply = reviewReplyRepository.findByReviewId(review.getId()).orElse(null);
            if (reply != null) {
                ReviewReplyDto replyDto = new ReviewReplyDto();
                replyDto.setId(reply.getId());
                replyDto.setReviewId(reply.getReviewId());
                replyDto.setInstructorId(reply.getInstructorId());
                replyDto.setContent(reply.getContent());
                replyDto.setCreatedAt(reply.getCreatedAt());
                replyDto.setUpdatedAt(reply.getUpdatedAt());
                return new ReviewResponse(dto.id(), dto.userId(), dto.courseId(), dto.content(), dto.rating(), replyDto, dto.reactions());
            }
            return dto;
        });
    }

    @Override
    public ReviewResponse getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        ReviewResponse dto = reviewMapper.toDto(review);
        ReviewReply reply = reviewReplyRepository.findByReviewId(review.getId()).orElse(null);
        if (reply != null) {
            ReviewReplyDto replyDto = new ReviewReplyDto();
            replyDto.setId(reply.getId());
            replyDto.setReviewId(reply.getReviewId());
            replyDto.setInstructorId(reply.getInstructorId());
            replyDto.setContent(reply.getContent());
            replyDto.setCreatedAt(reply.getCreatedAt());
            replyDto.setUpdatedAt(reply.getUpdatedAt());
            return new ReviewResponse(dto.id(), dto.userId(), dto.courseId(), dto.content(), dto.rating(), replyDto, dto.reactions());
        }
        return dto;
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
        reportRepository.save(Report.builder()
                .refId(reviewId)
                .type(ReportType.REVIEW)
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

    @Override
    public void createReviewReply(String reviewId, CreateReviewReplyRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        
        ApiResponse<CourseResponse> courseRes = courseService.getCourseById(review.getCourseId());
        if (courseRes == null || !courseRes.success() || courseRes.data() == null) {
            throw new NotFoundException("Course not found");
        }
        
        String instructorId = courseRes.data().instructor().id();
        if (!userId.equals(instructorId)) {
            throw new ForbiddenException(ErrorCode.INSTRUCTOR_ACCESS_REQUIRED, "Only the instructor of the course can reply to this review");
        }

        if (reviewReplyRepository.findByReviewId(reviewId).isPresent()) {
            throw new com.cinx.common.exception.AlreadyExistException(ErrorCode.RESOURCE_ALREADY_EXISTS, "Reply already exists for this review");
        }

        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setInstructorId(userId);
        reply.setContent(request.getContent());
        reviewReplyRepository.save(reply);
    }

    @Override
    public void updateReviewReply(String replyId, UpdateReviewReplyRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("Reply not found"));
        
        if (!reply.getInstructorId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "Not the owner of this reply");
        }

        reply.setContent(request.getContent());
        reviewReplyRepository.save(reply);
    }

    @Override
    public void deleteReviewReply(String replyId) {
        String userId = AuthenticationUtil.extractUserId();
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("Reply not found"));
        
        if (!reply.getInstructorId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "Not the owner of this reply");
        }

        reviewReplyRepository.delete(reply);
    }
}
