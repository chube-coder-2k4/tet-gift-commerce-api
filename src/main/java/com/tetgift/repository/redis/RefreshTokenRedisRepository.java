package com.tetgift.repository.redis;

import com.tetgift.model.redismodel.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository("refreshTokenRedisRepository")
public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, String> {

    void deleteByUsersId(Long userId);

    RefreshToken findByUsersId(Long userId);
}
