package com.tetgift.model.redisModel;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "otp_verify", timeToLive = 300)
public class OtpVerify {
    @Id
    private String email;
    private UUID userId;
    private String otp;
}
