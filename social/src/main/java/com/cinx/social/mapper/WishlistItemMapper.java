package com.cinx.social.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.social.dto.response.WishlistItemResponse;
import com.cinx.social.model.WishlistItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WishlistItemMapper extends BaseMapper<WishlistItem, WishlistItemResponse> {
}
