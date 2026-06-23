package com.cinx.social.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.social.dto.response.ReviewResponse;
import com.cinx.social.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper extends BaseMapper<Review, ReviewResponse> {
    @Override
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "reply", ignore = true)
    @Mapping(target = "reactions", ignore = true)
    ReviewResponse toDto(Review review);
}
