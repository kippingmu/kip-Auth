package xyz.kip.auth.manager.cache.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.kip.auth.manager.cache.CacheManager;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis cache manager implementation.
 * @author xiaoshichuan
 * @version 2026-03-07 17:42
 */
@Component
public class RedisCacheManager implements CacheManager {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // -------- String --------
    @Override
    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : type.cast(value);
    }

    @Override
    public void set(String key, Object value, long ttlSeconds) {
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void delete(Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public boolean expire(String key, long ttlSeconds) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds)));
    }

    @Override
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    @Override
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // -------- Hash --------
    @Override
    public <T> T hGet(String key, String hashKey, Class<T> type) {
        Object value = redisTemplate.opsForHash().get(key, hashKey);
        return value == null ? null : type.cast(value);
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public void hDelete(String key, String... hashKeys) {
        if (hashKeys != null && hashKeys.length > 0) {
            redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
        }
    }

    @Override
    public Map<String, Object> hGetAll(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        Map.Entry::getValue
                ));
    }

    @Override
    public boolean hExists(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }

    @Override
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    // -------- List --------
    @Override
    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    @Override
    public void rPush(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    @Override
    public <T> List<T> lRange(String key, long start, long end, Class<T> type) {
        List<Object> list = redisTemplate.opsForList().range(key, start, end);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(type::cast).toList();
    }

    @Override
    public void lTrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    @Override
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // -------- Set --------
    @Override
    public void sAdd(String key, Object... values) {
        if (values != null && values.length > 0) {
            redisTemplate.opsForSet().add(key, values);
        }
    }

    @Override
    public <T> Set<T> sMembers(String key, Class<T> type) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) {
            return Collections.emptySet();
        }
        return members.stream().map(type::cast).collect(Collectors.toSet());
    }

    @Override
    public void sRemove(String key, Object... values) {
        if (values != null && values.length > 0) {
            redisTemplate.opsForSet().remove(key, values);
        }
    }

    @Override
    public boolean sIsMember(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    @Override
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // -------- Sorted Set --------
    @Override
    public void zAdd(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    @Override
    public <T> Set<T> zRange(String key, long start, long end, Class<T> type) {
        Set<Object> range = redisTemplate.opsForZSet().range(key, start, end);
        if (range == null) {
            return Collections.emptySet();
        }
        return range.stream().map(type::cast).collect(Collectors.toSet());
    }

    @Override
    public <T> Set<T> zRangeByScore(String key, double min, double max, Class<T> type) {
        Set<Object> range = redisTemplate.opsForZSet().rangeByScore(key, min, max);
        if (range == null) {
            return Collections.emptySet();
        }
        return range.stream().map(type::cast).collect(Collectors.toSet());
    }

    @Override
    public void zRemove(String key, Object... values) {
        if (values != null && values.length > 0) {
            redisTemplate.opsForZSet().remove(key, values);
        }
    }

    @Override
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    @Override
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    // -------- 批量操作 --------
    @Override
    public void multiSet(Map<String, Object> entries, long ttlSeconds) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        redisTemplate.opsForValue().multiSet(entries);
        if (ttlSeconds > 0) {
            entries.keySet().forEach(key -> redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds)));
        }
    }

    @Override
    public List<Object> multiGet(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        List<Object> result = redisTemplate.opsForValue().multiGet(keys);
        return result != null ? result : Collections.emptyList();
    }
}
