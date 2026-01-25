package com.tetgift.service;

import com.tetgift.model.Users;

public interface UserService {

    Users findByUsername(String username);

}
