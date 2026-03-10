package com.example.shopping3.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        // 1. 字符串序列化器 (Key 使用 String)
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 2. JSON 序列化器 (Value 使用 JSON，支持对象)
        // 核心修改：创建 ObjectMapper 并开启默认类型支持，以替代 GenericJackson2JsonRedisSerializer
        ObjectMapper objectMapper = new ObjectMapper();
        // 解决反序列化时类型丢失问题 (替代 GenericJackson2JsonRedisSerializer 的自动类型处理)
        PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class) // 允许所有子类，根据实际安全需求可限制包名
                .build();
        objectMapper.activateDefaultTyping(validator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // 使用配置好 ObjectMapper 的 Jackson2JsonRedisSerializer
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 3. 设置序列化规则
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        // Spring 容器管理 Bean 时会自动调用 afterPropertiesSet，手动调用也可以
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
