package com.cinx.learning.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.learning.dto.response.UserStreakResponse;
import com.cinx.learning.model.UserStreak;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserStreakMapper extends BaseMapper<UserStreak, UserStreakResponse> {
}
