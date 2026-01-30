package com.tetgift.controller;

import com.tetgift.dto.request.UserRequest;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Validated
@Slf4j
@Tag(name = "User Controller", description = "APIs for user operations")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register User", description = "Register a new user in the system")
    public ResponseEntity<ResponseData<Long>> registerUser(@RequestBody @Valid UserRequest request) {
        Long userId = userService.saveUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseData<>(
                        HttpStatus.CREATED.value(),
                        "User registered successfully",
                        userId
                ));
    }
}
