package com.campusenroll.courseservice.config;

import com.campusenroll.courseservice.catalog.cache.CatalogCacheNames;
import com.campusenroll.courseservice.catalog.dto.AcademicPeriodResponse;
import com.campusenroll.courseservice.catalog.dto.CourseResponse;
import com.campusenroll.courseservice.catalog.dto.SectionResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
public class CacheConfiguration {

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(
            @Value("${app.cache.ttl-seconds:300}") long ttlSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttlSeconds))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
    }

    @Bean
    RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            RedisCacheConfiguration redisCacheConfiguration) {
        return builder -> builder
                .cacheDefaults(redisCacheConfiguration)
                .withCacheConfiguration(
                        CatalogCacheNames.COURSES,
                        catalogCacheConfiguration(redisCacheConfiguration, CourseResponse.class))
                .withCacheConfiguration(
                        CatalogCacheNames.ACADEMIC_PERIODS,
                        catalogCacheConfiguration(redisCacheConfiguration, AcademicPeriodResponse.class))
                .withCacheConfiguration(
                        CatalogCacheNames.SECTIONS,
                        catalogCacheConfiguration(redisCacheConfiguration, SectionResponse.class));
    }

    @Bean
    CacheErrorHandler cacheErrorHandler() {
        return new LoggingCacheErrorHandler();
    }

    RedisSerializer<Object> redisListValueSerializer(Class<?> elementType) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        JavaType javaType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elementType);

        return new Jackson2JsonRedisSerializer<>(objectMapper, javaType);
    }

    private RedisCacheConfiguration catalogCacheConfiguration(
            RedisCacheConfiguration baseConfiguration,
            Class<?> elementType) {
        return baseConfiguration.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(redisListValueSerializer(elementType)));
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
