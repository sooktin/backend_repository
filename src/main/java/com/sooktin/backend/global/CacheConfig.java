package com.sooktin.backend.global;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
public class CacheConfig  {
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration("userNote", userNoteCacheConfig())
                .withCacheConfiguration("careerCard", careerCardCacheConfig());
    }
    private RedisCacheConfiguration careerCardCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)); //캐시 ttl 2분
    }
    private RedisCacheConfiguration userNoteCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)); //캐시 ttl 2분
    }
}
