package com.cinx.cart.service.cart;

import com.cinx.cart.dto.request.AddToCartRequest;
import com.cinx.cart.dto.response.CartItemResponse;
import java.util.List;

public interface ICartService {
    List<CartItemResponse> getCart(String userId);
    void addToCart(String userId, AddToCartRequest request);
    void removeFromCart(String userId, String itemId);
    void removeAllFromCartByIds(String userId, List<String> itemIds);
    void clearCart(String userId);
}
