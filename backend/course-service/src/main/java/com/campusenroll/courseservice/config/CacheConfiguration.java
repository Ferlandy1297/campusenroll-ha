package com.campusenroll.courseservice.config;

import com.campusenroll.courseservice.catalog.cache.CatalogCacheNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
public class CacheConfiguration {

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(@Value("${app.cache.ttl-seconds:300}") long ttlSeconds) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttlSeconds))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper)));
    }

    @Bean
    RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            RedisCacheConfiguration redisCacheConfiguration) {
        return builder -> builder
                .cacheDefaults(redisCacheConfiguration)
                .withCacheConfiguration(CatalogCacheNames.COURSES, redisCacheConfiguration)
                .withCacheConfiguration(CatalogCacheNames.ACADEMIC_PERIODS, redisCacheConfiguration)
                .withCacheConfiguration(CatalogCacheNames.SECTIONS, redisCacheConfiguration);
    }

    @Bean
    CacheErrorHandler cacheErrorHandler() {
        return new LoggingCacheErrorHandler();
    }

    private static final class LoggingCacheErrorHandler implements CacheErrorHandler {

        private static final Logger log = LoggerFactory.getLogger(LoggingCacheErrorHandler.class);

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn(
                    "Redis cache get failed for cache '{}' and key '{}'. Falling back to database access.",
                    cacheName(cache),
                    key,
                    exception);
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn(
                    "Redis cache put failed for cache '{}' and key '{}'. Continuing without cached value.",
                    cacheName(cache),
                    key,
                    exception);
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Redis cache eviction failed for cache '{}' and key '{}'.", cacheName(cache), key, exception);
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("Redis cache clear failed for cache '{}'.", cacheName(cache), exception);
        }

        private String cacheName(Cache cache) {
            return cache == null ? "unknown" : cache.getName();
        }
    }
}
