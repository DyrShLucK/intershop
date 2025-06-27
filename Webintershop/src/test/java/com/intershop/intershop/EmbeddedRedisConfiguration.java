package com.intershop.intershop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class EmbeddedRedisConfiguration {

    @Bean
    public RedisServer redisServer() throws IOException {
        RedisServer redisServer = new RedisServer();
        redisServer.start();
        return redisServer;
    }
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
                .cacheDefaults(
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(1))
                                .disableCachingNullValues()
                )
                .build();
    }
}