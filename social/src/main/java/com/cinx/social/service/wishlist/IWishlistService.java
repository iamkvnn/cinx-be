package com.cinx.social.service.wishlist;

import com.cinx.social.dto.request.AddToWishlistRequest;
import com.cinx.social.dto.response.WishlistItemResponse;

import java.util.List;

public interface IWishlistService {
    List<WishlistItemResponse> getWishlistByUserId();
    void addToWishlist(AddToWishlistRequest request);
    void removeFromWishlist(String courseId);
}
