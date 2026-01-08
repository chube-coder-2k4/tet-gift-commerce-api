package com.tetgift.repository.redis;

import com.tetgift.model.redisModel.OtpVerify;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("otpVerifyRedisRepository")
public interface OtpVerifyRepository extends CrudRepository<OtpVerify, String> {
}
