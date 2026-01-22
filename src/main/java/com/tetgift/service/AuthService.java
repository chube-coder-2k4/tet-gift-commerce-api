package com.tetgift.service;

import com.tetgift.dto.request.LoginRequest;
import com.tetgift.dto.request.RegisterRequest;
import com.tetgift.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}