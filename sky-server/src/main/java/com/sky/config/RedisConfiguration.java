package com.sky.config;

import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 解释说明：
 * 当前配置类不是必须的，因为 Spring Boot 框架会自动装配 RedisTemplate 对象，但是默认的key序列化器为
 * JdkSerializationRedisSerializer，导致我们存到Redis中后的数据和原始数据有差别，故设置为
 * StringRedisSerializer序列化器（key）和 GenericJackson2JsonRedisSerializer（value，支持复杂对象）。
 */
@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象...");
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //设置redis value的序列化器（使用自定义的JacksonObjectMapper，支持LocalDateTime等时间类型）
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(new JacksonObjectMapper());
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        //设置hash key的序列化器
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //设置hash value的序列化器
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        return redisTemplate;
    }
}
