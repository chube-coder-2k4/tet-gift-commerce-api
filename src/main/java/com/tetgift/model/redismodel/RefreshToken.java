package com.tetgift.model.redismodel;

import jakarta.persistence.Column;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "RefreshToken", timeToLive = 604800)
public class RefreshToken {
    @Id
    private String token;
    @Column(name = "user_id")
    private Long usersId;
    private boolean revoked = false;
}
