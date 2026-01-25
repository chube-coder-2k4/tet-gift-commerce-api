package com.tetgift.service;

import com.tetgift.model.redisModel.RefreshToken;

public interface RefreshTokenService {
    void deleteByUserId(Long userId);
    String saveRefreshToken(RefreshToken refreshToken);
    RefreshToken getByUserId(Long userId);
}
