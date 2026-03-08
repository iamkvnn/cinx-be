 package com.cinx.cart.controller;

import com.cinx.cart.dto.request.AddToCartRequest;
import com.cinx.cart.dto.response.CartItemResponse;
import com.cinx.cart.service.cart.ICartService;
import com.cinx.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final ICartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCart(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart retrieved successfully", cartService.getCart(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addToCart(@RequestHeader("X-User-Id") String userId, @RequestBody AddToCartRequest request) {
        cartService.addToCart(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item added to cart successfully", null));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@RequestHeader("X-User-Id") String userId, @PathVariable String itemId) {
        cartService.removeFromCart(userId, itemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item removed from cart successfully", null));
    }

    @DeleteMapping("/ids")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@RequestHeader("X-User-Id") String userId, @RequestParam List<String> itemIds) {
        cartService.removeAllFromCartByIds(userId, itemIds);
        return ResponseEntity.ok(new ApiResponse<>(true, "Items removed from cart successfully", null));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@RequestHeader("X-User-Id") String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart cleared successfully", null));
    }
}
