package com.cinx.cart.service.cart;

import com.cinx.cart.dto.request.AddToCartRequest;
import com.cinx.cart.dto.response.CartItemResponse;
import java.util.List;

public interface ICartService {
    List<CartItemResponse> getCart();
    void addToCart(AddToCartRequest request);
    void removeFromCart(String itemId);
    void removeAllFromCartByIds(List<String> itemIds);
    void clearCart();
}
