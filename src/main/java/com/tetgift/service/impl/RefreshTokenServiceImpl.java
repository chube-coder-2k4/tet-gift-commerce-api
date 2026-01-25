package com.tetgift.service.impl;

import com.tetgift.model.redisModel.RefreshToken;
import com.tetgift.repository.redis.RefreshTokenRedisRepository;
import com.tetgift.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRedisRepository refreshTokenRepository;
    @Override
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUsersId(userId);
    }

    @Override
    public String saveRefreshToken(RefreshToken refreshToken) {
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        return savedToken.getToken();
    }

    @Override
    public RefreshToken getByUserId(Long userId) {
        return refreshTokenRepository.findByUsersId(userId);
    }
}
