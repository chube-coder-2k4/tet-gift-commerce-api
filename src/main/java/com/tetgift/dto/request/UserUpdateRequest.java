package com.tetgift.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class  UserUpdateRequest {
        private String fullName;

        @Email(message = "Email is not valid")
        private String email;

        private String phone;

        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;
}
