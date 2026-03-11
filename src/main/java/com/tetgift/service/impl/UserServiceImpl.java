package com.tetgift.service.impl;

import com.tetgift.dto.request.UserRequest;
import com.tetgift.dto.request.UserUpdateRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.UserResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public UserResponse findByUsername(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return usersMapper.toResponse(user);
    }

    @Override
    @Transactional
    public Long saveUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidDataException("Email already exists");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new InvalidDataException("Username already exists");
        }

        Users user = usersMapper.toEntity(request);

        // Set default role = USER
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role USER not found"));
        user.setRole(userRole);

        user.setCreatedBy(authenticationUtils.getCurrentUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Users savedUser = userRepository.save(user);

        // Generate and send OTP
        String otp = otpVerifyService.generateOtp();
        otpVerifyService.saveOtp(request.getEmail(), otp);
        mailService.sendOtpMail(savedUser.getEmail(), otp);

        return savedUser.getId();
    }

    @Override
    @Transactional
    public Long updateUser(Long userId, UserUpdateRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate unique email
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new InvalidDataException("Email already exists");
            }
        }

        // Validate unique username
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new InvalidDataException("Username already exists");
            }
        }

        // Actually update user fields using MapStruct @MappingTarget
        usersMapper.updateUserFromRequest(request, user);
        user.setUpdatedBy(authenticationUtils.getCurrentUserId());

        Users updatedUser = userRepository.save(user);
        return updatedUser.getId();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public UserResponse findById(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return usersMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // page is 0-indexed in Spring Data
        Pageable pageable = PageRequest.of(Math.max(page, 0), size, sort);
        Page<Users> userPage = userRepository.findAll(pageable);

        List<UserResponse> responses = userPage.getContent().stream()
                .map(usersMapper::toResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .data(responses)
                .pageNo(page)
                .pageSize(size)
                .totalItems(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }
}
