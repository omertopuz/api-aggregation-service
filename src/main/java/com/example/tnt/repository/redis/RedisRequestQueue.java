package com.example.tnt.repository.redis;

import com.example.tnt.model.ApiRequest;
import com.example.tnt.repository.RequestQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class RedisRequestQueue implements RequestQueue {
    private final RedisTemplate<String, ApiRequest> requestRedisTemplate;

    public RedisRequestQueue(LettuceConnectionFactory lettuceConnectionFactory) {
        this.requestRedisTemplate = new RedisTemplate<>();
        this.requestRedisTemplate.setConnectionFactory(lettuceConnectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        objectMapper.registerModule(timeModule);
        Jackson2JsonRedisSerializer<ApiRequest> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer(objectMapper, ApiRequest.class);

        requestRedisTemplate.setKeySerializer(new StringRedisSerializer());
        requestRedisTemplate.setHashKeySerializer(new StringRedisSerializer());
        requestRedisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        requestRedisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        requestRedisTemplate.setDefaultSerializer(jackson2JsonRedisSerializer);
        requestRedisTemplate.afterPropertiesSet();
    }

    @Override
    public void pushRequest(String queueName, ApiRequest apiRequest) {
        this.requestRedisTemplate.opsForList().leftPush(queueName,apiRequest);
    }

    @Override
    public ApiRequest pollRequest(String queueName) {
        return requestRedisTemplate.opsForList().rightPop(queueName);
    }

    @Override
    public ApiRequest peekRequest(String queueName) {
        ApiRequest apiRequest = requestRedisTemplate.opsForList().rightPop(queueName);
        if (apiRequest != null)
            requestRedisTemplate.opsForList().rightPush(queueName,apiRequest);
        return apiRequest;
    }

    @Override
    public int size(String queueName) {
        return requestRedisTemplate.opsForList().size(queueName).intValue();
    }

    @Override
    public Set<String> getQueueNames() {
        return requestRedisTemplate.opsForList().getOperations().keys("*");
    }
}
