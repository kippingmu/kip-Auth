package xyz.kip.auth.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for synchronous operations.
 * Configures RedisTemplate with proper serialization strategies.
 *
 * @author xiaoshichuan
 */
@Configuration
public class RedisConfig {

    /**
     * Configure RedisTemplate bean with custom serializers.
     * <ul>
     *   <li>Key serializer: StringRedisSerializer - stores keys as UTF-8 strings</li>
     *   <li>Value serializer: GenericJackson2JsonRedisSerializer - stores values as JSON</li>
     *   <li>Hash key serializer: StringRedisSerializer - stores hash keys as UTF-8 strings</li>
     *   <li>Hash value serializer: GenericJackson2JsonRedisSerializer - stores hash values as JSON</li>
     * </ul>
     *
     * @param connectionFactory Redis connection factory
     * @return configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
