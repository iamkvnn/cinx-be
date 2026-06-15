package com.cinx.social.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
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
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Wishlist retrieved successfully", wishlistService.getWishlistByUserId(userId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> addToWishlist(@RequestBody AddToWishlistRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        wishlistService.addToWishlist(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course added to wishlist successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> removeFromWishlist(@RequestParam String courseId) {
        String userId = AuthenticationUtil.extractUserId();
        wishlistService.removeFromWishlist(userId, courseId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course removed from wishlist successfully", null));
    }
}
