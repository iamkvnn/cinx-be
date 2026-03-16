package com.cinx.social.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.social.dto.response.ReviewResponse;
import com.cinx.social.model.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper extends BaseMapper<Review, ReviewResponse> {

}
