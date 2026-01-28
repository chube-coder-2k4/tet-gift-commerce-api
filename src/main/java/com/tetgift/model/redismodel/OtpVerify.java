package com.tetgift.model.redismodel;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "otp_verify", timeToLive = 300)
public class OtpVerify {
    @Id
    private String email;
    private Long userId;
    private String otp;
}
