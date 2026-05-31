package com.elcom.adminconsolebackend.config;

import com.elcom.adminconsolebackend.dto.IgnoreRequestDTO;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String password;

//    @Value("${spring.redis.ssl}")
//    private boolean ssl;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Tạo Standalone Connection tới Redis
        RedisStandaloneConfiguration redisStandaloneConfig = new RedisStandaloneConfiguration();
        redisStandaloneConfig.setHostName(redisHost);
        redisStandaloneConfig.setPort(redisPort);
        redisStandaloneConfig.setPassword(RedisPassword.of(password));
        LettuceClientConfiguration lettuceClientConfig = null;
//        if (ssl) {
//            lettuceClientConfig = LettuceClientConfiguration.builder().useSsl().build();
//        } else {
        lettuceClientConfig = LettuceClientConfiguration.builder().build();
//        }
        return new LettuceConnectionFactory(redisStandaloneConfig, lettuceClientConfig);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Serializable> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // tạo ra một RedisTemplate
        // Với Key là Object || String
        // Value là Object || String
        // RedisTemplate giúp spring-boot thao tác với Redis

        /*RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;*/
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        configureSerializers(redisTemplate);
        return redisTemplate;
    }

    @Bean("redisTemplateForHash")
    public RedisTemplate<String, IgnoreRequestDTO> redisTemplateForHash(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, IgnoreRequestDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Tạo ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        return template;
    }

    private void configureSerializers(RedisTemplate<String, Serializable> redisTemplate) {
        RedisSerializer<String> serializerKey = new StringRedisSerializer();
        redisTemplate.setKeySerializer(serializerKey);
        redisTemplate.setHashKeySerializer(serializerKey);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        om.registerModule(new Hibernate5Module());
        jackson2JsonRedisSerializer.setObjectMapper(om);

        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
    }
}
