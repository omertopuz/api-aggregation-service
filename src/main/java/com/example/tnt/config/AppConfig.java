package com.example.tnt.config;

import com.example.tnt.repository.RequestQueue;
import com.example.tnt.repository.ResponseMap;
import com.example.tnt.repository.inmemory.InMemoryRequestQueue;
import com.example.tnt.repository.inmemory.InMemoryResponseMap;
import com.example.tnt.repository.redis.RedisRequestQueue;
import com.example.tnt.repository.redis.RedisResponseMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Slf4j
@Configuration
public class AppConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.password}")
    private String password;
    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Bean
    @ConditionalOnProperty(prefix = "app", name = "cache", havingValue = "in-memory")
    public ResponseMap buildResponseMap(){
        return new InMemoryResponseMap();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app", name = "cache", havingValue = "in-memory")
    public RequestQueue buildRequestQueue(){
        return new InMemoryRequestQueue();
    }

    @Bean
    @ConditionalOnProperty(prefix = "app", name = "cache", havingValue = "redis")
    public ResponseMap buildResponseMapRedis(){
        return new RedisResponseMap(redisConnectionFactory());
    }

    @Bean
    @ConditionalOnProperty(prefix = "app", name = "cache", havingValue = "redis")
    public RequestQueue buildRequestQueueRedis(){
        return new RedisRequestQueue(redisConnectionFactory());
    }

    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("redisHost: {}",redisHost);
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        configuration.setPassword(RedisPassword.of(password));

        return new LettuceConnectionFactory(configuration, LettuceClientConfiguration.builder().build());
    }
}
