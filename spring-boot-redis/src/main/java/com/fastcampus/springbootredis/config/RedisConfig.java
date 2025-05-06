package com.fastcampus.springbootredis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    // Caused by: org.springframework.data.redis.serializer.SerializationException:
    // Could not write JSON: Java 8 date/time type `java.time.LocalDateTime` not supported by default:
    // add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling (through reference chain: com.fastcampus.springbootredis.model.User["createdAt"])
    // LocalDateTime을 JSON으로 변환할 때, 기본적으로 지원하지 않기 때문에 ObjectMapper에 JavaTimeModule을 등록해줘야 한다.
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        return mapper;
    }

    // Redis CLI에서 명령어롤 사용했던 것처럼, JAVA에서는 RedisTemplate을 사용하여 Redis에 접근할 수 있다.
    @Bean
    public RedisTemplate<String, Object> redisTemplate(ObjectMapper mapper, RedisConnectionFactory connectionFactory) { // ObjectMapper를 주입받아 사용
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer 설정 > 키는 문자열 형태이므로 StringRedisSerializer 사용
        template.setKeySerializer(new StringRedisSerializer());

        // Value Serializer 설정 > JSON 객체를 넣을 것이기 때문에 Jackson2JsonRedisSerializer 사용
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        serializer.setObjectMapper(mapper);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }
}
