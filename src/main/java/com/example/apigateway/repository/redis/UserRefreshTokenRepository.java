package com.example.apigateway.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void setValues(String key, String value) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();;
        values.set(key, value);
    }

    public void setValues(String key, String value, Duration duration) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();;
        values.set(key, value, duration);
    }


    @Transactional(readOnly = true)
    public String getValues(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.get(key);
        return values.get(key);
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
}
