package com.tetgift.service;

import com.tetgift.dto.request.UserRequest;
import com.tetgift.dto.request.UserUpdateRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.UserResponse;


public interface UserService {

    UserResponse findByUsername(String username);

    Long saveUser(UserRequest request);

    Long updateUser(Long userId, UserUpdateRequest request);

    void deleteUser(Long userId);

    UserResponse findById(Long userId);

    PageResponse<UserResponse> getUsers(int page, int size, String sortBy, String sortDir);

}
