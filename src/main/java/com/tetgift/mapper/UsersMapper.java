package com.tetgift.mapper;


import com.tetgift.dto.request.UserRequest;
import com.tetgift.dto.response.UserResponse;
import com.tetgift.model.Users;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UsersMapper {
    Users toEntity(UserRequest user);
    UserResponse toResponse(Users user);
}
