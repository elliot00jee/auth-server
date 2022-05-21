package com.example.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RedisService {
    private final RedisTemplate redisTemplate;

    public void setRedisTimeout(String jwtId, long timeoutSeconds) {
        redisTemplate.expire(jwtId, timeoutSeconds, TimeUnit.SECONDS);
    }

    public void remove(String jwtId) {
        redisTemplate.delete(jwtId);
    }

    public void update(String key, String value, long timeoutSeconds){
        redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
