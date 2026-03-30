package com.cinx.social.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.social.dto.request.AddToWishlistRequest;
import com.cinx.social.dto.response.WishlistItemResponse;
import com.cinx.social.service.wishlist.IWishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishlist")
public class WishlistController {
    private final IWishlistService wishlistService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getWishlist() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist retrieved successfully", wishlistService.getWishlistByUserId()));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> addToWishlist(@RequestBody AddToWishlistRequest request) {
        wishlistService.addToWishlist(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course added to wishlist successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> removeFromWishlist(@RequestParam String courseId) {
        wishlistService.removeFromWishlist(courseId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course removed from wishlist successfully", null));
    }
}
