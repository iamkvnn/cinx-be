package com.cinx.social.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.dto.request.CreateReportReviewRequest;
import com.cinx.social.dto.request.CreateReviewReactionRequest;
import com.cinx.social.dto.request.CreateReviewRequest;
import com.cinx.social.dto.request.UpdateReviewRequest;
import com.cinx.social.service.review.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final IReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getReviewsByCourseId(@RequestParam String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Reviews retrieved successfully", reviewService.getReviewsByCourseId(courseId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createReview(@RequestBody CreateReviewRequest request) {
        reviewService.createReview(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review created successfully", null));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> updateReview(@PathVariable String reviewId, @RequestBody UpdateReviewRequest request) {
        reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review updated successfully", null));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review deleted successfully", null));
    }

    @PostMapping("/{reviewId}/report")
    public ResponseEntity<ApiResponse<?>> reportReview(@PathVariable String reviewId, @RequestBody CreateReportReviewRequest request) {
        reviewService.reportReview(reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review reported successfully", null));
    }

    @PostMapping("/{reviewId}/react")
    public ResponseEntity<ApiResponse<?>> reactReview(@PathVariable String reviewId, @RequestBody CreateReviewReactionRequest request) {
        reviewService.reactReview(reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review reaction added successfully", null));
    }
}
