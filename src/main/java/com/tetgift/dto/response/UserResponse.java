package com.tetgift.dto.response;

import com.tetgift.model.Address;
import com.tetgift.model.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String username;
    private Set<Address> addresses = new HashSet<>();
    private Set<Role> roles = new HashSet<>();
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
