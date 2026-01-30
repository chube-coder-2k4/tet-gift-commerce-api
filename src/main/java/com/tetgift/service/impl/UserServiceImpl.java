package com.tetgift.service.impl;

import com.tetgift.dto.request.UserRequest;
import com.tetgift.dto.request.UserUpdateRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.exception.InvalidDataException;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.mapper.UsersMapper;
import com.tetgift.model.Role;
import com.tetgift.model.Users;
import com.tetgift.repository.jpa.RoleRepository;
import com.tetgift.repository.jpa.UserRepository;
import com.tetgift.service.MailService;
import com.tetgift.service.OtpVerifyService;
import com.tetgift.service.UserService;
import com.tetgift.util.AuthenticationUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UsersMapper usersMapper;
    private final AuthenticationUtils authenticationUtils;
    private final PasswordEncoder passwordEncoder;
    private final OtpVerifyService otpVerifyService;
    private final MailService mailService;
    private final RoleRepository roleRepository;

    @Override
    public Users findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public Long saveUser(UserRequest request){
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidDataException("Email already exists");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new InvalidDataException("Username already exists");
        }
        Users user = usersMapper.toEntity(request);
        Set<Role> role = roleRepository.findByName("USER");
        user.setRoles(role);
        user.setCreatedBy(authenticationUtils.getCurrentUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Users savedUser = userRepository.save(user);
        String otp = otpVerifyService.generateOtp();
        otpVerifyService.saveOtp(request.getEmail(), otp);
        mailService.sendOtpMail(savedUser.getEmail(), otp);
        return savedUser.getId();
    }

    @Override
    public Long updateUser(Long userId, UserUpdateRequest request) {
        return 0L;
    }

    @Override
    public void deleteUser(Long userId) {

    }

    @Override
    public Users findById(Long userId) {
        return null;
    }

    @Override
    public PageResponse<Users> getAllUsers(int page, int size, String sortBy, String sortDir) {
        return null;
    }
}
