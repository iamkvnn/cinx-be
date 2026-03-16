package com.cinx.social.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.dto.request.AddToWishlistRequest;
import com.cinx.social.service.wishlist.IWishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishlist")
public class WishlistController {
    private final IWishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getWishlist() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist retrieved successfully", wishlistService.getWishlistByUserId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addToWishlist(@RequestBody AddToWishlistRequest request) {
        wishlistService.addToWishlist(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course added to wishlist successfully", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> removeFromWishlist(@RequestParam String courseId) {
        wishlistService.removeFromWishlist(courseId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course removed from wishlist successfully", null));
    }
}
