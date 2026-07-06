package com.cinx.social.service.review;

import com.cinx.common.exception.NotFoundException;
import com.cinx.social.client.EnrollmentClient;
import com.cinx.social.client.UserClient;
import com.cinx.social.messaging.CourseReviewEventPublisher;
import com.cinx.social.model.ReportType;
import com.cinx.social.model.Review;
import com.cinx.social.repository.ReportRepository;
import com.cinx.social.repository.ReviewReactionRepository;
import com.cinx.social.repository.ReviewReplyRepository;
import com.cinx.social.repository.ReviewRepository;
import com.cinx.social.service.course.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ReviewReactionRepository reviewReactionRepository;
    @Mock
    private ReviewReplyRepository reviewReplyRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private EnrollmentClient enrollmentClient;
    @Mock
    private UserClient userClient;
    @Mock
    private CourseReviewEventPublisher reviewEventPublisher;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void deleteReviewCleansChildrenReportsAndUpdatesCourseRating() {
        Review review = review("review-1", "owner-1", "course-1");
        when(reviewRepository.findById("review-1")).thenReturn(Optional.of(review));
        when(reviewRepository.getAverageRatingByCourseId("course-1")).thenReturn(4.0);

        reviewService.deleteReview("owner-1", "review-1");

        verify(reviewReactionRepository).deleteByReviewId("review-1");
        verify(reviewReplyRepository).deleteByReviewId("review-1");
        verify(reportRepository).deleteByRefIdAndType("review-1", ReportType.REVIEW);
        verify(reviewRepository).delete(review);
        verify(courseService).updateCourseRating("course-1", 4.0);
    }

    @Test
    void deleteReviewKeepsOwnerCheck() {
        Review review = review("review-1", "owner-1", "course-1");
        when(reviewRepository.findById("review-1")).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview("other-user", "review-1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Review not found");
    }

    @Test
    void deleteReviewByAdminBypassesOwnerCheckAndUsesSameCleanup() {
        Review review = review("review-1", "owner-1", "course-1");
        when(reviewRepository.findById("review-1")).thenReturn(Optional.of(review));
        when(reviewRepository.getAverageRatingByCourseId("course-1")).thenReturn(4.0);

        reviewService.deleteReviewByAdmin("review-1");

        verify(reviewReactionRepository).deleteByReviewId("review-1");
        verify(reviewReplyRepository).deleteByReviewId("review-1");
        verify(reportRepository).deleteByRefIdAndType("review-1", ReportType.REVIEW);
        verify(reviewRepository).delete(review);
        verify(courseService).updateCourseRating("course-1", 4.0);
    }

    private Review review(String id, String userId, String courseId) {
        Review review = Review.builder()
                .userId(userId)
                .courseId(courseId)
                .rating(5.0)
                .content("Review content")
                .build();
        review.setId(id);
        return review;
    }
}
