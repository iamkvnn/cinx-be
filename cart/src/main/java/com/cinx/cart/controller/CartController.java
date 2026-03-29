 package com.cinx.cart.controller;

import com.cinx.cart.dto.request.AddToCartRequest;
import com.cinx.cart.dto.response.CartItemResponse;
import com.cinx.cart.service.cart.ICartService;
import com.cinx.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final ICartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCart() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart retrieved successfully", cartService.getCart()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addToCart(@RequestBody AddToCartRequest request) {
        cartService.addToCart(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item added to cart successfully", null));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable String itemId) {
        cartService.removeFromCart(itemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item removed from cart successfully", null));
    }

    @DeleteMapping("/ids")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@RequestParam List<String> itemIds) {
        cartService.removeAllFromCartByIds(itemIds);
        return ResponseEntity.ok(new ApiResponse<>(true, "Items removed from cart successfully", null));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(new ApiResponse<>(true, "Cart cleared successfully", null));
    }
}
