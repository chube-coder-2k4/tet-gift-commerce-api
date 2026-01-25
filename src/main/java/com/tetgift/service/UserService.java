package com.tetgift.service;

import com.tetgift.model.Users;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public interface UserService {

    Users findByUsername(String username);

}
