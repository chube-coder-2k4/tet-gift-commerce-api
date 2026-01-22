package com.tetgift.service.impl;

import com.tetgift.dto.request.LoginRequest;
import com.tetgift.dto.request.RegisterRequest;
import com.tetgift.dto.response.LoginResponse;
import com.tetgift.enums.LoginType;
import com.tetgift.model.Role;
import com.tetgift.model.Users;
import com.tetgift.repository.jpa.RoleRepository;
import com.tetgift.repository.jpa.UserRepository;
import com.tetgift.service.AuthService;
import com.tetgift.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name("USER")
                                .description("Default user role")
                                .build()
                ));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        Users user = Users.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(LoginType.LOCAL)
                .isActive(true)
                .roles(roles)
                .build();

        Users savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return LoginResponse.builder()
                .accessToken(token)
                .userId(savedUser.getId())
                .role(userRole.getName())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Users user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);
        String role = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");

        return LoginResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .role(role)
                .build();
    }
}