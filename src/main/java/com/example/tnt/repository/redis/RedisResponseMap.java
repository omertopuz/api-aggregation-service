package com.example.tnt.repository.redis;

import com.example.tnt.model.ApiResponse;
import com.example.tnt.repository.ResponseMap;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;

public class RedisResponseMap implements ResponseMap {
    private final RedisTemplate<UUID, ApiResponse> responseRedisTemplate;

    public RedisResponseMap(LettuceConnectionFactory lettuceConnectionFactory) {
        this.responseRedisTemplate = new RedisTemplate<>();
        this.responseRedisTemplate.setConnectionFactory(lettuceConnectionFactory);
        responseRedisTemplate.afterPropertiesSet();
    }

    @Override
    public void addResponse(ApiResponse apiResponse) {
        responseRedisTemplate.opsForSet().add(apiResponse.getTraceId(),apiResponse);
    }

    @Override
    public ApiResponse consumeResponse(UUID traceId) {
        ApiResponse response = null;
        while (response == null) {
            response = responseRedisTemplate.opsForSet().pop(traceId);
        }
        return response;
    }
}
