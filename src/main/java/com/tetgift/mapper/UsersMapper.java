package com.tetgift.mapper;

import com.tetgift.dto.request.UserRequest;
import com.tetgift.dto.request.UserUpdateRequest;
import com.tetgift.dto.response.UserResponse;
import com.tetgift.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsersMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    Users toEntity(UserRequest request);

    @Mapping(source = "role.name", target = "roleName")
    UserResponse toResponse(Users user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    void updateUserFromRequest(UserUpdateRequest request, @MappingTarget Users user);
}
