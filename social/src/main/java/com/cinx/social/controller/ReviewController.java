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
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.social.dto.request.CreateReviewReplyRequest;
import com.cinx.social.dto.request.UpdateReviewReplyRequest;
import org.springframework.data.domain.Page;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final IReviewService reviewService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<ReviewResponse>> getReviewsByCourseId(
            @RequestParam String courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        Page<ReviewResponse> reviewPage = reviewService.getReviewsByCourseId(courseId, page, size, sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(reviewPage));
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

    @Operation(summary = "Reply to review (Instructor only)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{reviewId}/replies")
    public ResponseEntity<ApiResponse<?>> createReviewReply(@PathVariable String reviewId, @RequestBody CreateReviewReplyRequest request) {
        reviewService.createReviewReply(reviewId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review reply created successfully", null));
    }

    @Operation(summary = "Update review reply (Instructor only)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<?>> updateReviewReply(@PathVariable String replyId, @RequestBody UpdateReviewReplyRequest request) {
        reviewService.updateReviewReply(replyId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review reply updated successfully", null));
    }

    @Operation(summary = "Delete review reply (Instructor only)", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<ApiResponse<?>> deleteReviewReply(@PathVariable String replyId) {
        reviewService.deleteReviewReply(replyId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Review reply deleted successfully", null));
    }
}
