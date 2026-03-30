package com.cinx.social.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.dto.request.CreateReportReviewRequest;
import com.cinx.social.dto.request.CreateReviewReactionRequest;
import com.cinx.social.dto.request.CreateReviewRequest;
import com.cinx.social.dto.request.UpdateReviewRequest;
import com.cinx.social.dto.response.ReviewResponse;
import com.cinx.social.service.review.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final IReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByCourseId(@RequestParam String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Reviews retrieved successfully", reviewService.getReviewsByCourseId(courseId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createReview(@RequestBody CreateReviewRequest request) {
        reviewService.createReview(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review created successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> updateReview(@PathVariable String reviewId, @RequestBody UpdateReviewRequest request) {
        reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review updated successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review deleted successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{reviewId}/report")
    public ResponseEntity<ApiResponse<?>> reportReview(@PathVariable String reviewId, @RequestBody CreateReportReviewRequest request) {
        reviewService.reportReview(reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review reported successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{reviewId}/react")
    public ResponseEntity<ApiResponse<?>> reactReview(@PathVariable String reviewId, @RequestBody CreateReviewReactionRequest request) {
        reviewService.reactReview(reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review reaction added successfully", null));
    }
}
