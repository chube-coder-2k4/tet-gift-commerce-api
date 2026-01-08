package com.tetgift.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.tetgift.repository.jpa")
@EnableRedisRepositories(basePackages = "com.tetgift.repository.redis")
public class RepositoryConfig {
}
