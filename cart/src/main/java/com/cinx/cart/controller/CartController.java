 package com.cinx.cart.controller;

import com.cinx.cart.dto.request.AddToCartRequest;
import com.cinx.cart.dto.response.CartItemResponse;
import com.cinx.cart.service.cart.ICartService;
import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final ICartService cartService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCart() {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart retrieved successfully", cartService.getCart(userId)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addToCart(@RequestBody AddToCartRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        cartService.addToCart(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item added to cart successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable String itemId) {
        String userId = AuthenticationUtil.extractUserId();
        cartService.removeFromCart(userId, itemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item removed from cart successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        String userId = AuthenticationUtil.extractUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart cleared successfully", null));
    }
}
