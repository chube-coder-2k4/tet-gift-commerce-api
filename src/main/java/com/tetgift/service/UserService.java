package com.tetgift.service;

import com.tetgift.dto.request.UserRequest;
import com.tetgift.dto.request.UserUpdateRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.model.Users;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface UserService {

    Users findByUsername(String username);

    Long saveUser(UserRequest request) throws MessagingException, UnsupportedEncodingException;

    Long updateUser(Long userId, UserUpdateRequest request);

    void deleteUser(Long userId);

    Users findById(Long userId);

    PageResponse<Users> getAllUsers(int page, int size, String sortBy, String sortDir);
}
