package com.yashaswi.weatherapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yashaswi.weatherapi.dtos.WeatherResponse;
import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.*;
import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.sentinel.master}")
    private String sentinelMaster;

    @Value("${spring.data.redis.sentinel.nodes}")
    private String sentinelNodes;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Create Sentinel configuration
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
                .master(sentinelMaster);

        // Parse and add sentinel nodes
        for (String node : sentinelNodes.split(",")) {
            String[] parts = node.trim().split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            sentinelConfig.sentinel(host, port);
        }

        // Configure Lettuce client with proper settings
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .commandTimeout(Duration.ofSeconds(5))
                .build();

        return new LettuceConnectionFactory(sentinelConfig, clientConfig);
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<WeatherResponse> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, WeatherResponse.class);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(12))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }
}
