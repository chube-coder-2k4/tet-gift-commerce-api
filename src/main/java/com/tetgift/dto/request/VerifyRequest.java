package com.tetgift.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyRequest {
    private String email;
    private String otp;
}
