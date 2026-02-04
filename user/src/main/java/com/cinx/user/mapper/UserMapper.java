package com.cinx.user.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.user.dto.UpdateProifileRequest;
import com.cinx.user.dto.UserDto;
import com.cinx.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<User, UserDto>, UpdateMapper<User, UpdateProifileRequest> {
}
