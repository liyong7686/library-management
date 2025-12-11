package com.library.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory(Environment environment) {
        String host = environment.getProperty("spring.redis.host", "localhost");
        int port = Integer.parseInt(environment.getProperty("spring.redis.port", "6379"));
        int database = Integer.parseInt(environment.getProperty("spring.redis.database", "0"));
        String password = environment.getProperty("spring.redis.password");
        
        logger.info("配置 Redis 连接: host={}, port={}, database={}, password={}", 
            host, port, database, password != null && !password.isEmpty() ? "已设置" : "未设置");
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);
        
        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setValidateConnection(false); // 延迟验证，避免启动时连接失败
        
        try {
            factory.afterPropertiesSet();
            logger.info("Redis 连接工厂已创建");
        } catch (Exception e) {
            logger.error("创建 Redis 连接工厂失败: {}", e.getMessage(), e);
            throw e;
        }
        
        return factory;
    }
    
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置 ObjectMapper 以支持 Java 8 时间类型
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 关键：启用默认类型信息，确保反序列化时能正确识别类型
        // 使用 NON_FINAL 模式，对所有非 final 类添加类型信息
        // 这样 GenericJackson2JsonRedisSerializer 就能正确反序列化为原始类型，而不是 LinkedHashMap
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
            com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );
        
        // 使用 GenericJackson2JsonRedisSerializer
        // 它会自动在 JSON 中添加 @class 类型信息，确保反序列化时能正确转换为原始类型
        // 这是解决 LinkedHashMap 转换问题的关键
        org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer serializer = 
            new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 配置 Redis 缓存
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 缓存过期时间 1 小时
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues(); // 不缓存 null 值
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
    
    /**
     * Redis 连接测试 Bean
     * 在应用启动时测试 Redis 连接
     */
    @Bean
    public org.springframework.boot.CommandLineRunner redisConnectionTest(
            RedisConnectionFactory connectionFactory, Environment environment) {
        return args -> {
            try {
                org.springframework.data.redis.connection.RedisConnection connection = connectionFactory.getConnection();
                connection.ping();
                connection.close();
                logger.info("✅ Redis 连接测试成功");
            } catch (Exception e) {
                logger.error("❌ Redis 连接测试失败: {}", e.getMessage());
                logger.error("请检查 Redis 服务是否启动，以及配置是否正确");
                logger.error("Redis 配置: host={}, port={}", 
                    environment.getProperty("spring.redis.host", "localhost"),
                    environment.getProperty("spring.redis.port", "6379"));
            }
        };
    }
}
