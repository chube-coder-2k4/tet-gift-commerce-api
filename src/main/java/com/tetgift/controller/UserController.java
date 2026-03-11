package com.tetgift.controller;

import com.tetgift.dto.request.UserRequest;
import com.tetgift.dto.request.UserUpdateRequest;
import com.tetgift.dto.response.PageResponse;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.dto.response.UserResponse;
import com.tetgift.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Users", description = "Retrieve a paginated list of all users")
    public ResponseEntity<ResponseData<PageResponse<UserResponse>>> getAllUser(@RequestParam int page,
                                                                               @RequestParam int size,
                                                                               @RequestParam String sortBy,
                                                                               @RequestParam String sortDir) {
        PageResponse<UserResponse> users = userService.getUsers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetched users successfully",
                users
        ));

    }

    @PutMapping("/{id}")
    @Operation(summary = "Update User", description = "Update an existing user's information")
    public ResponseEntity<ResponseData<Long>> updateUser(@PathVariable Long id,
                                                         @RequestBody @Valid UserUpdateRequest request) {
        Long updatedUserId = userService.updateUser(id, request);
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "User updated successfully",
                updatedUserId
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete User", description = "Delete a user by their ID")
    public ResponseEntity<ResponseData<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "User deleted successfully",
                "User with ID " + id + " has been deleted"
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get User by ID", description = "Retrieve a user's information by their ID")
    public ResponseEntity<ResponseData<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetched user successfully",
                user
        ));
    }
}
