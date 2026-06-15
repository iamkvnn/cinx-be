package com.cinx.social.service.wishlist;

import com.cinx.social.dto.request.AddToWishlistRequest;
import com.cinx.social.dto.response.WishlistItemResponse;

import java.util.List;

public interface IWishlistService {
    List<WishlistItemResponse> getWishlistByUserId(String userId);
    void addToWishlist(String userId, AddToWishlistRequest request);
    void removeFromWishlist(String userId, String courseId);
}
